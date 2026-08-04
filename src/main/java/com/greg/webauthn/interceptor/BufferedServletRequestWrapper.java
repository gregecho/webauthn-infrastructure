package com.greg.webauthn.interceptor;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Wraps an {@link HttpServletRequest} so the body can be read more than once.
 * Needed because both the JWT interceptor and the controller both consume the body.
 * Registered via {@link com.greg.webauthn.filter.WebFilter}.
 */
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {
            @Override public int read()                             { return bais.read(); }
            @Override public boolean isFinished()                  { return bais.available() == 0; }
            @Override public boolean isReady()                     { return true; }
            @Override public void setReadListener(ReadListener rl) { /* no-op */ }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    public String getBody() {
        return body;
    }
}
