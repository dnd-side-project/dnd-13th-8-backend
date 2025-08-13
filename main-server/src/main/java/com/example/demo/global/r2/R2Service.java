package com.example.demo.global.r2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class R2Service {

    private final S3Client s3;
    private final S3Presigner presigner;

    @Value("${r2.bucket}") private String bucket;
    @Value("${r2.presign-expire-minutes:10}") private int expireMinute;


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

    public String getPresignedUrl(String key) {
        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        PresignedGetObjectRequest pre = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expireMinute))
                        .getObjectRequest(get)
                        .build());
        return pre.url().toString();
    }
}
