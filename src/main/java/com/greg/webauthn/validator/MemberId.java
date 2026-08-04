package com.greg.webauthn.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE,
         ElementType.PARAMETER, ElementType.TYPE})
@Constraint(validatedBy = MemberIdValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MemberId {
    String message() default "invalidMemberId|Required Parameter memberId is Not valid";
    Class<?>[] groups() default {};
    /** When true, an empty/null value passes validation (field is optional). */
    boolean isEmpty() default false;
    Class<? extends Payload>[] payload() default {};
}
