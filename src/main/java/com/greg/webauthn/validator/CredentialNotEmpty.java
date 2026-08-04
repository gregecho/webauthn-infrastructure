package com.greg.webauthn.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
         ElementType.PARAMETER, ElementType.TYPE})
@Constraint(validatedBy = CredentialNotEmptyValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface CredentialNotEmpty {
    String message() default "missingCredential|Required Parameter credential is Not Present";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
