package com.greg.webauthn.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Validates a member/user ID.
 *
 * <p>In the original Hyatt service this delegated to
 * {@code com.hyatt.pai.service.util.shared.validators.UserIdValidator.isMemberId()}.
 * Here we apply a sensible default: non-empty, 1–64 alphanumeric characters.
 * Adjust the pattern to match your actual member-ID format.
 */
@Component
public class MemberIdValidator implements ConstraintValidator<MemberId, String> {

    // Matches: 1–64 alphanumeric characters (adjust as needed)
    private static final String MEMBER_ID_PATTERN = "^[a-zA-Z0-9]{1,64}$";

    private boolean isEmpty;

    @Override
    public void initialize(MemberId annotation) {
        this.isEmpty = annotation.isEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Optional field — empty is allowed when isEmpty = true
        if (isEmpty && !StringUtils.hasText(value)) {
            return true;
        }
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return value.matches(MEMBER_ID_PATTERN);
    }
}
