package com.greg.webauthn.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CredentialNotEmptyValidator implements ConstraintValidator<CredentialNotEmpty, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(value);
            // Reject an explicitly empty JSON object {}
            return !(node.isObject() && node.isEmpty());
        } catch (Exception e) {
            // Non-JSON strings (e.g. plain text) pass this check; format validators follow
            return true;
        }
    }
}
