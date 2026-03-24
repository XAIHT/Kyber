package com.xaiht.kyber.crypto;

public final class AttestationEnvelope {

    private final KyberSecurityLevel securityLevel;
    private final String verifierFingerprint;
    private final String encapsulationBase64;
    private final String macBase64;
    private final String contextBase64;

    public AttestationEnvelope(KyberSecurityLevel securityLevel, String verifierFingerprint, String encapsulationBase64,
            String macBase64, String contextBase64) {
        this.securityLevel = securityLevel;
        this.verifierFingerprint = verifierFingerprint;
        this.encapsulationBase64 = encapsulationBase64;
        this.macBase64 = macBase64;
        this.contextBase64 = contextBase64;
    }

    public KyberSecurityLevel getSecurityLevel() {
        return securityLevel;
    }

    public String getVerifierFingerprint() {
        return verifierFingerprint;
    }

    public String getEncapsulationBase64() {
        return encapsulationBase64;
    }

    public String getMacBase64() {
        return macBase64;
    }

    public String getContextBase64() {
        return contextBase64;
    }
}
