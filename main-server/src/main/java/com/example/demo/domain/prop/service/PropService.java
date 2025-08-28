package com.example.demo.domain.prop.service;

import com.example.common.error.code.PropErrorCode;
import com.example.common.error.code.UserErrorCode;
import com.example.common.error.exception.PropException;
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
    public PropResponse saveProp(String userId, String theme, MultipartFile file) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(()-> new UserException(UserErrorCode.USER_NOT_FOUND));

        String imageKey = r2Service.newKey(file.getOriginalFilename());     // 버킷 키 생성
        try {
            r2Service.upload(file.getBytes(), file.getContentType(), imageKey); // R2에 저장
        } catch (IOException e) {
            throw new PropException(PropErrorCode.PROP_R2_ERROR);
        }
        Prop prop = Prop.builder()
                .user(user)
                .theme(theme)
                .imageKey(imageKey)
                .build();
        Prop saved = propRepository.save(prop);

        String imageUrl = r2Service.getPresignedUrl(imageKey);

        return PropResponse.builder()
                .propId(saved.getId())
                .theme(saved.getTheme())
                .imageUrl(imageUrl)
                .build();
    }

    public GetPropListResponseDto findPropListByUserId (String userId) {
        if (!usersRepository.existsById(userId)) {
            throw new UserException(UserErrorCode.USER_NOT_FOUND);
        }
        List<Prop> propList = propRepository.findAllByUsersId(userId);
        List<PropResponse> propResponsesList = propList.stream()
                .map(p -> PropResponse.builder()
                        .propId(p.getId())
                        .theme(p.getTheme())
                        .imageUrl(getPropImageUrl(p.getImageKey()))
                        .build()
                ).toList();

        return new GetPropListResponseDto(propResponsesList);
    }

    @Transactional
    public void deletePropById (String userId, Long propId) {
        Prop prop = propRepository.findByIdAndUsersId(propId, userId)
                .orElseThrow(()-> new PropException(PropErrorCode.PROP_NOT_FOUND));

        r2Service.delete(prop.getImageKey());
        propRepository.delete(prop);
    }

    public String getPropImageUrl (String imageKey) {
        return r2Service.getPresignedUrl(imageKey);
    }
}
