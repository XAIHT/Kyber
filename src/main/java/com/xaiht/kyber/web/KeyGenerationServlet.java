package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.KyberKeyMaterial;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.service.KyberKeyService;

@WebServlet("/keys/generate")
public class KeyGenerationServlet extends BaseKyberServlet {

    @Inject
    private KyberKeyService keyService;

    @Override
    protected String getViewPath() {
        return "/key-generator.jsp";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletSupport.validateCsrfToken(request);

            String securityLevelValue = ServletSupport.requireNonBlankParameter(request, "kyberSecurityLevel", 16);
            request.setAttribute("selectedSecurityLevel", securityLevelValue);

            KyberSecurityLevel securityLevel = KyberSecurityLevel.fromFormValue(securityLevelValue);
            KyberKeyMaterial generatedKeys = keyService.generateKeyPair(securityLevel);

            request.setAttribute("generatedKeys", generatedKeys);
            forwardSuccess(request, response);
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }
}
