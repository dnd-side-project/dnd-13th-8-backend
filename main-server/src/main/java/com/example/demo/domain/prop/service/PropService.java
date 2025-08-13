package com.example.demo.domain.prop.service;

import com.example.common.error.code.CommonErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.R2Exception;
import com.example.common.error.exception.UserException;
import com.example.demo.domain.prop.dto.response.GetPropListResponseDto;
import com.example.demo.domain.prop.dto.response.PropResponse;
import com.example.demo.domain.prop.entity.Prop;
import com.example.demo.domain.prop.repository.PropRepository;
import com.example.demo.domain.user.entity.Users;
import com.example.demo.domain.user.repository.UsersRepository;
import com.example.demo.global.r2.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PropService {

    private final R2Service r2Service;
    private final PropRepository propRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void saveProp(Long userId, MultipartFile file) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        String imageKey = r2Service.newKey(file.getOriginalFilename());     // 버킷 키 생성
        try {
            r2Service.upload(file.getBytes(), file.getContentType(), imageKey); // R2에 저장
        } catch (IOException e) {
            throw new R2Exception("R2 서버 오류입니다", CommonErrorCode.INTERNAL_ERROR);
        }

        Prop prop = Prop.builder()
                .user(user)
                .imageKey(imageKey)
                .build();
        propRepository.save(prop);
    }

    public GetPropListResponseDto findPropListByUserId (Long userId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        List<Prop> propList = propRepository.findAllByUser_Id(userId);
        List<PropResponse> propResponsesList = propList.stream()
                .map(p -> new PropResponse(
                    p.getId(),
                    getPropImageUrl(p.getImageKey())   // imageKey → presigned URL
                )).toList();

        return new GetPropListResponseDto(propResponsesList);
    }

    public String getPropImageUrl (String imageKey) {
        return r2Service.getPresignedUrl(imageKey);
    }
}
