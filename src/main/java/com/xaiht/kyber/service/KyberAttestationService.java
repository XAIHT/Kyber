package com.xaiht.kyber.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;

import com.xaiht.kyber.crypto.AttestationEnvelope;
import com.xaiht.kyber.crypto.CryptoOperationException;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.crypto.PostQuantumProviderRegistry;
import com.xaiht.kyber.crypto.ValidationException;
import com.xaiht.kyber.crypto.VerificationResult;

public class KyberAttestationService {

    private static final String KEM_ALGORITHM = "KYBER";
    private static final String KEM_SHARED_SECRET_ALGORITHM = "AES";
    private static final int SHARED_SECRET_BITS = 256;
    private static final int MAC_KEY_BYTES = 32;
    private static final String MAC_ALGORITHM = "HmacSHA256";

    private final SecureRandom secureRandom = new SecureRandom();
    private final KyberKeyService keyService;

    public KyberAttestationService() {
        this(new KyberKeyService());
    }

    public KyberAttestationService(KyberKeyService keyService) {
        this.keyService = keyService;
    }

    public AttestationEnvelope attest(KyberSecurityLevel securityLevel, String verifierPublicKeyBase64, String payload,
            String context) {
        PublicKey publicKey = keyService.decodePublicKey(verifierPublicKeyBase64, securityLevel);

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEM_ALGORITHM,
                    PostQuantumProviderRegistry.provider());
            keyGenerator.init(new KEMGenerateSpec(publicKey, KEM_SHARED_SECRET_ALGORITHM, SHARED_SECRET_BITS),
                    secureRandom);

            SecretKeyWithEncapsulation sharedSecret = (SecretKeyWithEncapsulation) keyGenerator.generateKey();
            byte[] mac = computeMac(sharedSecret.getEncoded(), payload, context);

            return new AttestationEnvelope(securityLevel, keyService.fingerprint(publicKey),
                    Base64.getEncoder().encodeToString(sharedSecret.getEncapsulation()),
                    Base64.getEncoder().encodeToString(mac),
                    Base64.getEncoder().encodeToString(context.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to generate the Kyber attestation envelope.", ex);
        }
    }

    public VerificationResult verify(KyberSecurityLevel securityLevel, String verifierPrivateKeyBase64, String payload,
            String context, String encapsulationBase64, String macBase64) {
        PrivateKey privateKey = keyService.decodePrivateKey(verifierPrivateKeyBase64, securityLevel);
        byte[] encapsulation = decodeBase64(encapsulationBase64, "encapsulation");
        byte[] expectedMac = decodeBase64(macBase64, "MAC");

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEM_ALGORITHM,
                    PostQuantumProviderRegistry.provider());
            keyGenerator.init(new KEMExtractSpec(privateKey, encapsulation, KEM_SHARED_SECRET_ALGORITHM,
                    SHARED_SECRET_BITS));
            SecretKey sharedSecret = keyGenerator.generateKey();

            byte[] computedMac = computeMac(sharedSecret.getEncoded(), payload, context);
            boolean verified = MessageDigest.isEqual(expectedMac, computedMac);

            return new VerificationResult(securityLevel, keyService.fingerprint(privateKey), verified,
                    Base64.getEncoder().encodeToString(computedMac));
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to verify the Kyber attestation envelope.", ex);
        }
    }

    private byte[] computeMac(byte[] sharedSecret, String payload, String context) throws GeneralSecurityException {
        byte[] info = ("XaihtKyber-Attestation|" + context).getBytes(StandardCharsets.UTF_8);
        byte[] macKey = hkdf(sharedSecret, info);

        try {
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(new SecretKeySpec(macKey, MAC_ALGORITHM));
            mac.update(payload.getBytes(StandardCharsets.UTF_8));
            mac.update((byte) 0);
            mac.update(context.getBytes(StandardCharsets.UTF_8));
            return mac.doFinal();
        } finally {
            Arrays.fill(sharedSecret, (byte) 0);
            Arrays.fill(macKey, (byte) 0);
        }
    }

    private byte[] hkdf(byte[] inputKeyMaterial, byte[] info) {
        HKDFBytesGenerator generator = new HKDFBytesGenerator(new SHA256Digest());
        generator.init(new HKDFParameters(inputKeyMaterial, null, info));

        byte[] result = new byte[MAC_KEY_BYTES];
        generator.generateBytes(result, 0, result.length);
        return result;
    }

    private byte[] decodeBase64(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException("The " + fieldName + " field is required.");
        }

        try {
            return Base64.getDecoder().decode(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("The " + fieldName + " field must contain valid Base64 data.", ex);
        }
    }
}
