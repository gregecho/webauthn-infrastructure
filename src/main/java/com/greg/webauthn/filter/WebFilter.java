package com.greg.webauthn.filter;

import com.greg.webauthn.interceptor.BufferedServletRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * Wraps incoming requests with {@link BufferedServletRequestWrapper} so the
 * request body can be read multiple times (once by the JWT interceptor,
 * once by the controller / Jackson).
 *
 * Applied to {@code /webauthn/v1/*} via {@link com.greg.webauthn.config.SecurityConfig}.
 */
@Order(-1)
@jakarta.servlet.annotation.WebFilter(filterName = "webFilter")
public class WebFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) { /* no-op */ }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpReq) {
            chain.doFilter(new BufferedServletRequestWrapper(httpReq), response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() { /* no-op */ }
}
