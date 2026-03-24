package com.xaiht.kyber.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xaiht.kyber.crypto.CipherEnvelope;
import com.xaiht.kyber.crypto.KyberKeyMaterial;
import com.xaiht.kyber.crypto.KyberSecurityLevel;

public class KyberCipherServiceTest {

    private final KyberKeyService keyService = new KyberKeyService();
    private final KyberCipherService cipherService = new KyberCipherService(keyService);

    @Test
    public void cipherAndDecipherRoundTripAcrossAllSupportedLevels() {
        for (KyberSecurityLevel securityLevel : KyberSecurityLevel.values()) {
            KyberKeyMaterial keyMaterial = keyService.generateKeyPair(securityLevel);
            CipherEnvelope envelope = cipherService.cipher(securityLevel, keyMaterial.getPublicKeyBase64(),
                    "Confidential payload for " + securityLevel.getDisplayName(), "tenant=A1;scope=demo");

            String plaintext = cipherService.decipher(securityLevel, keyMaterial.getPrivateKeyBase64(),
                    envelope.getEncapsulationBase64(), envelope.getInitializationVectorBase64(),
                    envelope.getCipherTextBase64(), envelope.getAadBase64());

            assertEquals("Confidential payload for " + securityLevel.getDisplayName(), plaintext);
        }
    }
}
