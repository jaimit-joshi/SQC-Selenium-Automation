package com.automation.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CryptoUtil - AES-256-GCM encryption for test data.
 *
 * How it works:
 *   encrypt("hello") -> "BASE64_RANDOM_LOOKING_STRING"
 *   decrypt("BASE64_RANDOM_LOOKING_STRING") -> "hello"
 *
 * Key is read from environment variable: ENCRYPTION_KEY
 * Must be exactly 32 characters (= 256 bits).
 *
 * To set the key:
 *   Mac/Linux: export ENCRYPTION_KEY="MySecret32CharacterKeyHere12345"
 *   Windows:   set ENCRYPTION_KEY=MySecret32CharacterKeyHere12345
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    // Prefix to identify encrypted values
    // (so we don't try to decrypt plain text)
    public static final String ENC_PREFIX = "ENC:";

    /**
     * Get the 32-char key from:
     *   1. config.properties file (most reliable)
     *   2. JVM system property (-DENCRYPTION_KEY=...)
     *   3. Environment variable (export ENCRYPTION_KEY=...)
     */
    private static SecretKey getKey() {
        String keyStr = null;

        // Try 1: config.properties in project root
        try {
            String propPath =
                    System.getProperty("user.dir")
                            + "/config.properties";
            java.util.Properties props =
                    new java.util.Properties();
            props.load(new java.io.FileInputStream(
                    propPath));
            keyStr = props.getProperty("ENCRYPTION_KEY");
            if (keyStr != null && !keyStr.isEmpty()) {
                System.out.println(
                        "  Key loaded from config.properties");
            }
        } catch (Exception ignored) {}

        // Try 2: JVM system property
        if (keyStr == null || keyStr.isEmpty()) {
            keyStr = System.getProperty("ENCRYPTION_KEY");
        }

        // Try 3: Environment variable
        if (keyStr == null || keyStr.isEmpty()) {
            keyStr = System.getenv("ENCRYPTION_KEY");
        }

        if (keyStr == null || keyStr.isEmpty()) {
            throw new RuntimeException(
                    "ENCRYPTION_KEY not found!\n"
                            + "Create config.properties in "
                            + "project root with:\n"
                            + "ENCRYPTION_KEY="
                            + "Your32CharacterSecretKeyHere!!");
        }

        if (keyStr.length() != 32) {
            throw new RuntimeException(
                    "ENCRYPTION_KEY must be exactly "
                            + "32 characters (256 bits). "
                            + "Current length: "
                            + keyStr.length());
        }

        return new SecretKeySpec(
                keyStr.getBytes(), ALGORITHM);
    }

    /**
     * Encrypt a plain text string.
     * Returns: "ENC:" + Base64(IV + ciphertext)
     * Each call produces different output (random IV).
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKey key = getKey();

            // Random IV for each encryption
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, key,
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] encrypted =
                    cipher.doFinal(plainText.getBytes());

            // Prepend IV to ciphertext
            byte[] combined = ByteBuffer
                    .allocate(IV_BYTES + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return ENC_PREFIX
                    + Base64.getEncoder()
                    .encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt an encrypted string.
     * Input must start with "ENC:" prefix.
     * If input is NOT encrypted, returns it unchanged.
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null
                || encryptedText.isEmpty()) {
            return encryptedText;
        }

        // If not encrypted, return as-is
        if (!encryptedText.startsWith(ENC_PREFIX)) {
            return encryptedText;
        }

        try {
            SecretKey key = getKey();

            // Remove prefix and decode Base64
            String base64Part =
                    encryptedText.substring(
                            ENC_PREFIX.length());
            byte[] combined =
                    Base64.getDecoder().decode(base64Part);

            // Extract IV and ciphertext
            ByteBuffer buffer =
                    ByteBuffer.wrap(combined);
            byte[] iv = new byte[IV_BYTES];
            buffer.get(iv);
            byte[] ciphertext =
                    new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(GCM_TAG_BITS, iv));

            byte[] decrypted =
                    cipher.doFinal(ciphertext);

            return new String(decrypted);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Decryption failed: " + e.getMessage()
                            + "\nCheck ENCRYPTION_KEY is correct.",
                    e);
        }
    }

    /**
     * Check if a string is encrypted.
     */
    public static boolean isEncrypted(String text) {
        return text != null
                && text.startsWith(ENC_PREFIX);
    }
}