package com.greg.webauthn.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
         ElementType.PARAMETER, ElementType.TYPE})
@Constraint(validatedBy = RequestIdValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestId {
    String message() default "invalidRequestId|Required Parameter RequestId is Not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
