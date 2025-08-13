package com.example.demo.domain.prop.service;

import com.example.common.error.exception.NotFoundException;
import com.example.common.error.exception.R2Exception;
import com.example.demo.domain.prop.dto.response.GetPropListResponseDto;
import com.example.demo.domain.prop.dto.response.PropResponse;
import com.example.demo.domain.prop.entity.Prop;
import com.example.demo.domain.prop.repository.PropRepository;
import com.example.demo.domain.user.entity.User;
import com.example.demo.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public void saveProp(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new NotFoundException("회원 정보가 없습니다"));

        String imageKey = r2Service.newKey(file.getOriginalFilename());     // 버킷 키 생성
        try {
            r2Service.upload(file.getBytes(), file.getContentType(), imageKey); // R2에 저장
        } catch (IOException e) {
            throw new R2Exception("이미지 업로드 실패하였습니다");
        }

        Prop prop = Prop.builder()
                .user(user)
                .imageKey(imageKey)
                .build();
        propRepository.save(prop);
    }

    public GetPropListResponseDto findPropListByUserId (Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("회원 정보가 없습니다.");
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
