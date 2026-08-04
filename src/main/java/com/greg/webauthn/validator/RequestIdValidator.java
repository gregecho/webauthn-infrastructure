package com.greg.webauthn.validator;

import com.greg.webauthn.utils.RequestIdUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class RequestIdValidator implements ConstraintValidator<RequestId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return RequestIdUtils.validateRequestIdFormat(value);
    }
}
