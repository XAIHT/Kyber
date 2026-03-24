package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.service.KyberCipherService;

@WebServlet("/buffer/decipher")
public class BufferDecipherServlet extends BaseKyberServlet {

    private final KyberCipherService cipherService = new KyberCipherService();

    @Override
    protected String getViewPath() {
        return "/buffer-decipher.jsp";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletSupport.validateCsrfToken(request);

            String securityLevelValue = ServletSupport.requireNonBlankParameter(request, "kyberSecurityLevel", 16);
            String recipientPrivateKey = ServletSupport.requireNonBlankParameter(request, "recipientPrivateKey", 65536);
            String encapsulation = ServletSupport.requireNonBlankParameter(request, "encapsulation", 32768);
            String initializationVector = ServletSupport.requireNonBlankParameter(request, "initializationVector", 256);
            String ciphertext = ServletSupport.requireNonBlankParameter(request, "ciphertext", ServletSupport.MAX_TEXT_LENGTH);
            String aadBase64 = ServletSupport.requireParameter(request, "aadBase64", 32768);

            request.setAttribute("selectedSecurityLevel", securityLevelValue);
            request.setAttribute("recipientPrivateKey", recipientPrivateKey);
            request.setAttribute("encapsulation", encapsulation);
            request.setAttribute("initializationVector", initializationVector);
            request.setAttribute("ciphertext", ciphertext);
            request.setAttribute("aadBase64", aadBase64);

            KyberSecurityLevel securityLevel = KyberSecurityLevel.fromFormValue(securityLevelValue);
            String plaintext = cipherService.decipher(securityLevel, recipientPrivateKey, encapsulation,
                    initializationVector, ciphertext, aadBase64);

            request.setAttribute("plaintextResult", plaintext);
            forwardSuccess(request, response);
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }
}
