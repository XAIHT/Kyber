package com.xaiht.kyber.crypto;

public final class CipherEnvelope {

    private final KyberSecurityLevel securityLevel;
    private final String recipientFingerprint;
    private final String encapsulationBase64;
    private final String initializationVectorBase64;
    private final String cipherTextBase64;
    private final String aadBase64;

    public CipherEnvelope(KyberSecurityLevel securityLevel, String recipientFingerprint, String encapsulationBase64,
            String initializationVectorBase64, String cipherTextBase64, String aadBase64) {
        this.securityLevel = securityLevel;
        this.recipientFingerprint = recipientFingerprint;
        this.encapsulationBase64 = encapsulationBase64;
        this.initializationVectorBase64 = initializationVectorBase64;
        this.cipherTextBase64 = cipherTextBase64;
        this.aadBase64 = aadBase64;
    }

    public KyberSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getRecipientFingerprint() {
        return recipientFingerprint;
    }

    public String getEncapsulationBase64() {
        return encapsulationBase64;
    }

    public String getInitializationVectorBase64() {
        return initializationVectorBase64;
    }

    public String getCipherTextBase64() {
        return cipherTextBase64;
    }

    public String getAadBase64() {
        return aadBase64;
    }
}
