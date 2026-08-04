package com.greg.webauthn.validator;

import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * Enforces validation order: EmptyCheck → FormatCheck → Default.
 * Apply via {@code @Validated({ValidationCheckSequence.class})} on controllers.
 */
@GroupSequence({EmptyCheck.class, FormatCheck.class, Default.class})
public interface ValidationCheckSequence {
}
