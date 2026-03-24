package com.xaiht.kyber.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.xaiht.kyber.crypto.AttestationEnvelope;
import com.xaiht.kyber.crypto.KyberKeyMaterial;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.crypto.VerificationResult;

public class KyberAttestationServiceTest {

    private final KyberKeyService keyService = new KyberKeyService();
    private final KyberAttestationService attestationService = new KyberAttestationService(keyService);

    @Test
    public void attestationVerifiesAcrossAllSupportedLevels() {
        for (KyberSecurityLevel securityLevel : KyberSecurityLevel.values()) {
            KyberKeyMaterial keyMaterial = keyService.generateKeyPair(securityLevel);
            AttestationEnvelope envelope = attestationService.attest(securityLevel, keyMaterial.getPublicKeyBase64(),
                    "Payload for " + securityLevel.getDisplayName(), "transaction-42");

            VerificationResult result = attestationService.verify(securityLevel, keyMaterial.getPrivateKeyBase64(),
                    "Payload for " + securityLevel.getDisplayName(), "transaction-42",
                    envelope.getEncapsulationBase64(), envelope.getMacBase64());

            assertTrue(result.isVerified());
        }
    }

    @Test
    public void attestationRejectsTamperedPayload() {
        KyberKeyMaterial keyMaterial = keyService.generateKeyPair(KyberSecurityLevel.KYBER_768);
        AttestationEnvelope envelope = attestationService.attest(KyberSecurityLevel.KYBER_768,
                keyMaterial.getPublicKeyBase64(), "Authoritative payload", "approval-window");

        VerificationResult result = attestationService.verify(KyberSecurityLevel.KYBER_768,
                keyMaterial.getPrivateKeyBase64(), "Tampered payload", "approval-window",
                envelope.getEncapsulationBase64(), envelope.getMacBase64());

        assertFalse(result.isVerified());
    }
}
