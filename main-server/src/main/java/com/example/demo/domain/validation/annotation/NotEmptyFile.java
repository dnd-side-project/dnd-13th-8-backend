package com.example.demo.domain.validation.annotation;

import com.example.demo.domain.validation.validator.NotEmptyFileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyFileValidator.class)
public @interface NotEmptyFile {
    String message() default "파일이 비어 있습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
