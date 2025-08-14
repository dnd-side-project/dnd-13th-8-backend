package com.example.demo.domain.prop.dto.request;

import com.example.demo.domain.validation.annotation.NotEmptyFile;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

public record UploadPropRequestDto(@NotEmptyFile MultipartFile file) {
}
