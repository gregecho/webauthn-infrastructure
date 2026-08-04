package com.greg.webauthn.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
         ElementType.PARAMETER, ElementType.TYPE})
@Constraint(validatedBy = ChannelsValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Channel {
    String message() default "Invalid Channel.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
