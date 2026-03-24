package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.AttestationEnvelope;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.service.KyberAttestationService;

@WebServlet("/attestation/sign")
public class KyberSignerServlet extends BaseKyberServlet {

    private final KyberAttestationService attestationService = new KyberAttestationService();

    @Override
    protected String getViewPath() {
        return "/kyber-signer.jsp";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletSupport.validateCsrfToken(request);

            String securityLevelValue = ServletSupport.requireNonBlankParameter(request, "kyberSecurityLevel", 16);
            String verifierPublicKey = ServletSupport.requireNonBlankParameter(request, "verifierPublicKey", 32768);
            String payload = ServletSupport.requireParameter(request, "payload", ServletSupport.MAX_TEXT_LENGTH);
            String context = ServletSupport.optionalParameter(request, "context", 16384);

            request.setAttribute("selectedSecurityLevel", securityLevelValue);
            request.setAttribute("verifierPublicKey", verifierPublicKey);
            request.setAttribute("payload", payload);
            request.setAttribute("context", context);

            KyberSecurityLevel securityLevel = KyberSecurityLevel.fromFormValue(securityLevelValue);
            AttestationEnvelope result = attestationService.attest(securityLevel, verifierPublicKey, payload, context);

            request.setAttribute("attestationEnvelope", result);
            forwardSuccess(request, response);
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }
}
