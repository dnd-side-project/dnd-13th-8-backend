package com.example.demo.global.r2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2Service {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.deulak-base-url}")
    private String baseUrl;

    @Value("${r2.presign-expire-minutes:10}")
    private int expireMinute;


    public String newKey(String originalFilename) { // 버켓 키: prop/{uuid}.{ext}
        String ext = "bin";
        if (originalFilename != null) {
            int i = originalFilename.lastIndexOf('.');
            if (i >= 0 && i < originalFilename.length() - 1) {
                ext = originalFilename.substring(i + 1);
            }
        }
        return "prop/" + UUID.randomUUID() + "." + ext;
    }

    public void upload(byte[] bytes, String contentType, String key) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes));
    }

    public void delete(String key) {
        s3.deleteObject(
                DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key) // 업로드 시 사용했던 key
                .build()
        );
    }

    public String getPublicUrl (String key) {
        return baseUrl + "/" + key;
    }

    public String extractKey(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        // baseUrl 로 시작하는지 체크
        if (url.startsWith(baseUrl)) {
            return url.substring(baseUrl.length() + 1);
        }
        return null;
    }
}
