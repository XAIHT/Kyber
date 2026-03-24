package com.xaiht.kyber.web;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.xaiht.kyber.crypto.ValidationException;

public final class ServletSupport {

    public static final int MAX_TEXT_LENGTH = 262144;
    private static final String CSRF_SESSION_ATTRIBUTE = "xaihtKyber.csrfToken";

    private ServletSupport() {
    }

    public static void applySecurityHeaders(HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0L);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; style-src 'self' 'unsafe-inline'; form-action 'self'; base-uri 'self';");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Frame-Options", "DENY");
    }

    public static String ensureCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(CSRF_SESSION_ATTRIBUTE, token);
        }
        request.setAttribute("csrfToken", token);
        return token;
    }

    public static void validateCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute(CSRF_SESSION_ATTRIBUTE);
        String actual = request.getParameter("csrfToken");

        if (expected == null || actual == null || !expected.equals(actual)) {
            throw new ValidationException("The request could not be validated. Refresh the page and retry.");
        }
    }

    public static String requireParameter(HttpServletRequest request, String name, int maxLength) {
        String value = request.getParameter(name);
        if (value == null) {
            throw new ValidationException("The " + name + " parameter is required.");
        }
        if (value.length() > maxLength) {
            throw new ValidationException("The " + name + " parameter exceeds the allowed size.");
        }
        return value;
    }

    public static String requireNonBlankParameter(HttpServletRequest request, String name, int maxLength) {
        String value = requireParameter(request, name, maxLength);
        if (value.trim().isEmpty()) {
            throw new ValidationException("The " + name + " parameter is required.");
        }
        return value;
    }

    public static String optionalParameter(HttpServletRequest request, String name, int maxLength) {
        String value = request.getParameter(name);
        if (value == null) {
            return "";
        }
        if (value.length() > maxLength) {
            throw new ValidationException("The " + name + " parameter exceeds the allowed size.");
        }
        return value;
    }

    public static void forward(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        ensureCsrfToken(request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
        dispatcher.forward(request, response);
    }

    public static String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        String message = current.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return current.getClass().getName();
        }

        return current.getClass().getName() + ": " + message;
    }
}
