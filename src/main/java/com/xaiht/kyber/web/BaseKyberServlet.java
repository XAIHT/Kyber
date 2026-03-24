package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.CryptoOperationException;
import com.xaiht.kyber.crypto.ValidationException;

public abstract class BaseKyberServlet extends HttpServlet {

    protected abstract String getViewPath();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletSupport.applySecurityHeaders(response);
        ServletSupport.forward(request, response, getViewPath());
    }

    protected void forwardSuccess(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletSupport.applySecurityHeaders(response);
        ServletSupport.forward(request, response, getViewPath());
    }

    protected void handleFailure(HttpServletRequest request, HttpServletResponse response, Exception ex)
            throws ServletException, IOException {
        ServletSupport.applySecurityHeaders(response);
        getServletContext().log("Kyber servlet request failed for view " + getViewPath(), ex);

        if (ex instanceof ValidationException || ex instanceof CryptoOperationException) {
            request.setAttribute("errorMessage", ex.getMessage());
        } else {
            request.setAttribute("errorMessage", ServletSupport.rootCauseMessage(ex));
        }

        ServletSupport.forward(request, response, getViewPath());
    }
}
