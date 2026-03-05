package com.example.demo.domain.validation.annotation;

import com.example.demo.domain.validation.validator.CarouselRequestValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CarouselRequestValidator.class)
public @interface ValidCarouselRequest {
    String message() default "캐러셀 조회 Params가 올바르지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
