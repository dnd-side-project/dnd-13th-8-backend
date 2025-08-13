package com.example.demo.domain.validation.validator;

import com.example.demo.domain.validation.annotation.NotEmptyFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyFileValidator implements ConstraintValidator<NotEmptyFile, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext ctx) {
        return value != null && !value.isEmpty();
    }
}
