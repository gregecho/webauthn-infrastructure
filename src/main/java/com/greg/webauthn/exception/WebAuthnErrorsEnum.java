package com.greg.webauthn.exception;

import org.springframework.http.HttpStatus;

/**
 * Application-level WebAuthn error codes.
 * Each enum value carries an HTTP status and a default message,
 * and exposes helpers to throw {@link WebAuthnException} cleanly from any layer.
 */
public enum WebAuthnErrorsEnum {

    BAD_INPUT              ("Bad input",                           HttpStatus.BAD_REQUEST),
    CONFLICT               ("Conflict",                            HttpStatus.CONFLICT),
    CHALLENGE_NOT_FOUND    ("WebAuthn challenge not found",        HttpStatus.UNAUTHORIZED),
    REQUEST_NOT_FOUND      ("WebAuthn request not found",          HttpStatus.NOT_FOUND),
    CREDENTIAL_NOT_FOUND   ("WebAuthn credential not found",       HttpStatus.NOT_FOUND),
    REGISTRATION_FAILED    ("Registration failed",                 HttpStatus.UNAUTHORIZED),
    ASSERTION_FAILED       ("Assertion failed",                    HttpStatus.UNAUTHORIZED),
    SERVER_ERROR           ("Internal server error",               HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String     errMsg;
    private final HttpStatus httpStatus;

    WebAuthnErrorsEnum(String errMsg, HttpStatus httpStatus) {
        this.errMsg     = errMsg;
        this.httpStatus = httpStatus;
    }

    public void throwException() {
        throw new WebAuthnException(errMsg, httpStatus);
    }

    public void throwException(String extraMsg) {
        throw new WebAuthnException(errMsg + ": " + extraMsg, httpStatus);
    }

    public void throwExceptionIf(boolean condition) {
        if (condition) throwException();
    }

    public String getErrMsg()         { return errMsg; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
