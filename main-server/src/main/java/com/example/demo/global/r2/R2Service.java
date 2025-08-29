package com.example.demo.global.r2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.static-bucket}")
    private String staticBucket;

    @Value("${r2.static-base-url}")
    private String staticBaseUrl;

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

    public String getPresignedUrl(String key) {
        var get = GetObjectRequest.builder().bucket(bucket).key(key).build();
        PresignedGetObjectRequest pre = presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(expireMinute))
                        .getObjectRequest(get)
                        .build());
        return pre.url().toString();
    }

    public String newStaticKey(String originalFilename) { // 버켓 키: prop/{uuid}.{ext}
        String ext = "bin";
        if (originalFilename != null) {
            int i = originalFilename.lastIndexOf('.');
            if (i >= 0 && i < originalFilename.length() - 1) {
                ext = originalFilename.substring(i + 1);
            }
        }
        return "static/" + UUID.randomUUID() + "." + ext;
    }

    public void staticUpload(byte[] bytes, String contentType, String key) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(staticBucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(bytes));
    }

    public void staticDelete(String key) {
        s3.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(staticBucket)
                        .key(key) // 업로드 시 사용했던 key
                        .build()
        );
    }

    public String staticUrl(String key) {
        return staticBaseUrl + "/" + key;
    }

    public String extractStaticKey(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        // baseUrl 로 시작하는지 체크
        if (url.startsWith(staticBaseUrl)) {
            return url.substring(staticBaseUrl.length() + 1);
        }
        return null;
    }
}
