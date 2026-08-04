package com.greg.webauthn.validator;

import com.yubico.webauthn.data.PublicKeyCredential;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class AssertionCredentialValidator implements ConstraintValidator<AssertionCredential, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            PublicKeyCredential.parseAssertionResponseJson(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
