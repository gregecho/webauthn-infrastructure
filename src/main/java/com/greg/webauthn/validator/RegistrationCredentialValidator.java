package com.greg.webauthn.validator;

import com.yubico.webauthn.data.PublicKeyCredential;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class RegistrationCredentialValidator implements ConstraintValidator<RegistrationCredential, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            PublicKeyCredential.parseRegistrationResponseJson(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
