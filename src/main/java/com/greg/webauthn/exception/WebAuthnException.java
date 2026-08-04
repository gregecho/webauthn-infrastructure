package com.greg.webauthn.exception;

import org.springframework.http.HttpStatus;

public class WebAuthnException extends RuntimeException {

    private final HttpStatus status;

    public WebAuthnException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
