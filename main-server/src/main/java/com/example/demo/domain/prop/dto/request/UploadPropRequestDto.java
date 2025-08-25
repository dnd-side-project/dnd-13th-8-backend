package com.example.demo.domain.prop.dto.request;

import com.example.demo.domain.validation.annotation.NotEmptyFile;
import org.springframework.web.multipart.MultipartFile;

public record UploadPropRequestDto(String theme, @NotEmptyFile MultipartFile file) {
}
