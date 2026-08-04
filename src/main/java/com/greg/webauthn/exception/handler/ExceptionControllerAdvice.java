package com.greg.webauthn.exception.handler;

import com.greg.webauthn.exception.ErrorJson;
import com.greg.webauthn.exception.WebAuthnException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Hidden
@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(WebAuthnException.class)
    @ResponseBody
    public ResponseEntity<ErrorJson> handleWebAuthnException(WebAuthnException ex) {
        ErrorJson body = ErrorJson.newBuilder()
                .setStatus(ex.getStatus().value())
                .setTimeStamp(new Date())
                .setError(ex.getStatus().getReasonPhrase())
                .setMessage(ex.getMessage())
                .done();
        return new ResponseEntity<>(body, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorJson handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return processFieldErrors(ex.getBindingResult().getFieldErrors());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorJson handleHttpMessageConversion(HttpMessageConversionException ex) {
        return ErrorJson.newBuilder()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setTimeStamp(new Date())
                .setError("Bad Request")
                .setMessage("invalidParameter|JSON parse error")
                .done();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorJson handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> messages = new HashMap<>();
        boolean allPiped = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessageTemplate)
                .allMatch(s -> s.contains("|"));

        if (allPiped) {
            List<String> errorMessages = ex.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessageTemplate)
                    .collect(Collectors.toList());
            messages = errorMessages.stream().collect(Collectors.toMap(
                    s -> s.split("\\|")[0],
                    s -> s.split("\\|")[1],
                    (k1, k2) -> k1));
        } else {
            messages.put(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage());
        }

        return ErrorJson.newBuilder()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setTimeStamp(new Date())
                .setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .setMessages(messages)
                .done();
    }

    private ErrorJson processFieldErrors(List<FieldError> fieldErrors) {
        Map<String, String> messages = new HashMap<>();
        for (ObjectError error : fieldErrors) {
            if (error instanceof FieldError fe) {
                String msg = fe.getDefaultMessage();
                if (msg != null && msg.contains("|")) {
                    messages.put(msg.split("\\|")[0], msg.split("\\|")[1]);
                } else {
                    messages.put(fe.getField(), msg);
                }
            } else {
                messages.put(error.getCode(), error.getDefaultMessage());
            }
        }
        return ErrorJson.newBuilder()
                .setStatus(HttpStatus.BAD_REQUEST.value())
                .setTimeStamp(new Date())
                .setError(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .setMessages(messages)
                .setMessage(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .done();
    }
}
