package com.greg.webauthn.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Validates that a channel value is one of the recognised values.
 *
 * <p>In the original Hyatt service this delegated to
 * {@code com.hyatt.pai.service.util.shared.validators.ChannelValidator}.
 * Here we enforce a set of known channel values directly. Update {@link #VALID_CHANNELS}
 * to add new ones, or wire an external source at construction time.
 */
@Component
public class ChannelsValidator implements ConstraintValidator<Channel, String> {

    private static final Set<String> VALID_CHANNELS = Set.of(
            "WEB", "MOBILE", "IOS", "ANDROID", "TABLET"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        return VALID_CHANNELS.contains(value.toUpperCase());
    }
}
