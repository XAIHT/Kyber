package com.xaiht.kyber.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.crypto.VerificationResult;
import com.xaiht.kyber.service.KyberAttestationService;

@WebServlet("/attestation/verify")
public class KyberSignVerifierServlet extends BaseKyberServlet {

    private final KyberAttestationService attestationService = new KyberAttestationService();

    @Override
    protected String getViewPath() {
        return "/kyber-sign-verifier.jsp";
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServletSupport.validateCsrfToken(request);

            String securityLevelValue = ServletSupport.requireNonBlankParameter(request, "kyberSecurityLevel", 16);
            String verifierPrivateKey = ServletSupport.requireNonBlankParameter(request, "verifierPrivateKey", 65536);
            String payload = ServletSupport.requireParameter(request, "payload", ServletSupport.MAX_TEXT_LENGTH);
            String context = ServletSupport.optionalParameter(request, "context", 16384);
            String encapsulation = ServletSupport.requireNonBlankParameter(request, "encapsulation", 32768);
            String mac = ServletSupport.requireNonBlankParameter(request, "mac", 32768);

            request.setAttribute("selectedSecurityLevel", securityLevelValue);
            request.setAttribute("verifierPrivateKey", verifierPrivateKey);
            request.setAttribute("payload", payload);
            request.setAttribute("context", context);
            request.setAttribute("encapsulation", encapsulation);
            request.setAttribute("mac", mac);

            KyberSecurityLevel securityLevel = KyberSecurityLevel.fromFormValue(securityLevelValue);
            VerificationResult result = attestationService.verify(securityLevel, verifierPrivateKey, payload, context,
                    encapsulation, mac);

            request.setAttribute("verificationResult", result);
            forwardSuccess(request, response);
        } catch (Exception ex) {
            handleFailure(request, response, ex);
        }
    }
}
