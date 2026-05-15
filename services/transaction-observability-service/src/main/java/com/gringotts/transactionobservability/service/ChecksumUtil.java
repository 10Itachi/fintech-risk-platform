package com.gringotts.transactionobservability.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 =============================================================
 IMMUTABLE EVENT CHECKSUM UTILITY
 =============================================================

 Used for:

 - payload integrity verification
 - tamper detection
 - forensic validation
 - immutable event tracking

 SHA-256 chosen for:
 - deterministic hashing
 - wide compatibility
 - cryptographic stability
 =============================================================
 */
public final class ChecksumUtil {

    /*
     =========================================================
     HEX CHAR TABLE
     =========================================================
     */
    private static final char[] HEX_ARRAY =
            "0123456789abcdef".toCharArray();

    /*
     =========================================================
     UTILITY CLASS
     =========================================================
     */
    private ChecksumUtil() {
    }

    /*
     =========================================================
     SHA-256 HASH
     =========================================================
     */
    public static String sha256(String input) {

        if (input == null) {

            throw new IllegalArgumentException(
                    "Checksum input cannot be null"
            );
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            input.getBytes(StandardCharsets.UTF_8)
                    );

            return toHex(hash);

        } catch (NoSuchAlgorithmException ex) {

            /*
             =================================================
             SHOULD NEVER HAPPEN
             =================================================

             SHA-256 is guaranteed in JVM spec.
             */
            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable",
                    ex
            );
        }
    }

    /*
     =========================================================
     HEX CONVERSION
     =========================================================
     */
    private static String toHex(byte[] bytes) {

        char[] hexChars =
                new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {

            int value = bytes[i] & 0xFF;

            hexChars[i * 2] =
                    HEX_ARRAY[value >>> 4];

            hexChars[i * 2 + 1] =
                    HEX_ARRAY[value & 0x0F];
        }

        return new String(hexChars);
    }
}