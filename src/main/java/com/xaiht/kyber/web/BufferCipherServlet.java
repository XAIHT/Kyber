package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.CipherEnvelope;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.service.KyberCipherService;

@WebServlet("/buffer/cipher")
public class BufferCipherServlet extends BaseKyberServlet {

    private final KyberCipherService cipherService = new KyberCipherService();

    @Override
    protected String getViewPath() {
        return "/buffer-cipher.jsp";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletSupport.validateCsrfToken(request);

            String securityLevelValue = ServletSupport.requireNonBlankParameter(request, "kyberSecurityLevel", 16);
            String recipientPublicKey = ServletSupport.requireNonBlankParameter(request, "recipientPublicKey", 32768);
            String plaintext = ServletSupport.requireParameter(request, "plaintext", ServletSupport.MAX_TEXT_LENGTH);
            String aad = ServletSupport.optionalParameter(request, "aad", 16384);

            request.setAttribute("selectedSecurityLevel", securityLevelValue);
            request.setAttribute("recipientPublicKey", recipientPublicKey);
            request.setAttribute("plaintext", plaintext);
            request.setAttribute("aad", aad);

            KyberSecurityLevel securityLevel = KyberSecurityLevel.fromFormValue(securityLevelValue);
            CipherEnvelope result = cipherService.cipher(securityLevel, recipientPublicKey, plaintext, aad);

            request.setAttribute("cipherEnvelope", result);
            forwardSuccess(request, response);
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }
}
