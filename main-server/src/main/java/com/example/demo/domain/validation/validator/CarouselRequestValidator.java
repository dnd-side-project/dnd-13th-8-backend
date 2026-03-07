package com.example.demo.domain.validation.validator;

import com.example.demo.domain.playlist.dto.feed.CarouselRequest;
import com.example.demo.domain.validation.annotation.ValidCarouselRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CarouselRequestValidator implements ConstraintValidator<ValidCarouselRequest, CarouselRequest> {

    @Override
    public boolean isValid(CarouselRequest value, ConstraintValidatorContext ctx) {
        if (value == null) return true;

        boolean hasAnchor = value.anchorId() != null;
        boolean hasDirection = value.direction() != null;
        boolean hasCursor = value.cursor() != null;

        boolean isInit = !hasAnchor && !hasDirection && !hasCursor;
        boolean isAnchor = hasAnchor && !hasDirection && !hasCursor;
        boolean isMove = !hasAnchor && hasDirection && hasCursor;

        if (isInit || isAnchor || isMove) return true;

        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(
                "요청은 비어 있거나, anchorId만 포함하거나, direction+cursor만 포함해야 합니다."
        ).addConstraintViolation();

        return false;
    }
}
