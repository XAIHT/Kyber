package com.xaiht.kyber.service;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;

import com.xaiht.kyber.crypto.CipherEnvelope;
import com.xaiht.kyber.crypto.CryptoOperationException;
import com.xaiht.kyber.crypto.KyberSecurityLevel;
import com.xaiht.kyber.crypto.PostQuantumProviderRegistry;
import com.xaiht.kyber.crypto.ValidationException;

@Dependent
public class KyberCipherService {

    private static final String KEM_ALGORITHM = "KYBER";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_SIZE_BITS = 256;
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_BYTES = 12;

    private final SecureRandom secureRandom = new SecureRandom();
    private final KyberKeyService keyService;

    public KyberCipherService() {
        this(new KyberKeyService());
    }

    @Inject
    public KyberCipherService(KyberKeyService keyService) {
        this.keyService = keyService;
    }

    public CipherEnvelope cipher(KyberSecurityLevel securityLevel, String recipientPublicKeyBase64, String plaintext,
            String aad) {
        PublicKey publicKey = keyService.decodePublicKey(recipientPublicKeyBase64, securityLevel);

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEM_ALGORITHM,
                    PostQuantumProviderRegistry.provider());
            keyGenerator.init(new KEMGenerateSpec(publicKey, SYMMETRIC_ALGORITHM, AES_KEY_SIZE_BITS), secureRandom);

            SecretKeyWithEncapsulation secretKey = (SecretKeyWithEncapsulation) keyGenerator.generateKey();
            byte[] iv = new byte[GCM_IV_BYTES];
            secureRandom.nextBytes(iv);

            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] aadBytes = aad.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = encrypt(secretKey.getEncoded(), iv, plaintextBytes, aadBytes);

            return new CipherEnvelope(securityLevel, keyService.fingerprint(publicKey),
                    Base64.getEncoder().encodeToString(secretKey.getEncapsulation()),
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(ciphertextBytes),
                    Base64.getEncoder().encodeToString(aadBytes));
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to cipher the supplied buffer.", ex);
        }
    }

    public String decipher(KyberSecurityLevel securityLevel, String recipientPrivateKeyBase64, String encapsulationBase64,
            String initializationVectorBase64, String cipherTextBase64, String aadBase64) {
        PrivateKey privateKey = keyService.decodePrivateKey(recipientPrivateKeyBase64, securityLevel);
        byte[] encapsulation = decodeBase64(encapsulationBase64, "encapsulation");
        byte[] iv = decodeBase64(initializationVectorBase64, "initialization vector");
        byte[] ciphertext = decodeBase64(cipherTextBase64, "ciphertext");
        byte[] aad = decodeBase64(aadBase64, "associated data");

        if (iv.length != GCM_IV_BYTES) {
            throw new ValidationException("The initialization vector must be exactly 12 bytes after Base64 decoding.");
        }

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEM_ALGORITHM,
                    PostQuantumProviderRegistry.provider());
            keyGenerator.init(new KEMExtractSpec(privateKey, encapsulation, SYMMETRIC_ALGORITHM, AES_KEY_SIZE_BITS));
            SecretKey secretKey = keyGenerator.generateKey();

            byte[] plaintextBytes = decrypt(secretKey.getEncoded(), iv, ciphertext, aad);
            return new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException ex) {
            throw new CryptoOperationException("Unable to decipher the supplied buffer.", ex);
        }
    }

    private byte[] encrypt(byte[] keyBytes, byte[] iv, byte[] plaintext, byte[] aad) throws GeneralSecurityException {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, SYMMETRIC_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            if (aad.length > 0) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(plaintext);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    private byte[] decrypt(byte[] keyBytes, byte[] iv, byte[] ciphertext, byte[] aad) throws GeneralSecurityException {
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, SYMMETRIC_ALGORITHM);
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            if (aad.length > 0) {
                cipher.updateAAD(aad);
            }
            return cipher.doFinal(ciphertext);
        } finally {
            Arrays.fill(keyBytes, (byte) 0);
        }
    }

    private byte[] decodeBase64(String value, String fieldName) {
        if (value == null) {
            throw new ValidationException("The " + fieldName + " field is required.");
        }

        try {
            return Base64.getDecoder().decode(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("The " + fieldName + " field must contain valid Base64 data.", ex);
        }
    }
}
