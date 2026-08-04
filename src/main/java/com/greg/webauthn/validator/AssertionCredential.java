package com.greg.webauthn.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
         ElementType.PARAMETER, ElementType.TYPE})
@Constraint(validatedBy = AssertionCredentialValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AssertionCredential {
    String message() default "Invalid Credential.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
