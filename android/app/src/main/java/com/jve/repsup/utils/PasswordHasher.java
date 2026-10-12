package com.jve.repsup.utils;

import android.util.Base64;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Small password-hashing utility for Reps Up.
 *
 * Passwords are hashed with PBKDF2WithHmacSHA256 (from javax.crypto, so no extra
 * library is needed) using a random 16-byte salt. The salt and the derived hash
 * are each Base64-encoded and stored together as "salt:hash" in the
 * set_password / admin_password columns.
 *
 * Verifying re-derives the hash from the supplied password using the stored
 * salt and compares the two in constant time. Because the salt is random per
 * password, two users with the same password get different stored values, and
 * the comparison is case-sensitive (unlike the old plain-text column, which
 * used MySQL's case-insensitive collation).
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String SEPARATOR = ":";

    private PasswordHasher() {
        // Utility class - no instances.
    }

    /**
     * Hashes a plain-text password with a fresh random salt.
     *
     * @param plainPassword the user's plain-text password
     * @return a "salt:hash" string (both parts Base64-encoded) safe to store
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);

        byte[] hash = pbkdf2(plainPassword.toCharArray(), salt);

        String saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP);
        return saltB64 + SEPARATOR + hashB64;
    }

    /**
     * Verifies a plain-text password against a stored "salt:hash" value.
     *
     * @param plainPassword the password the user typed
     * @param stored        the stored "salt:hash" value from the database
     * @return true if the password matches, false otherwise (including when the
     * stored value is null or not in the expected format)
     */
    public static boolean verify(String plainPassword, String stored) {
        if (plainPassword == null || stored == null) {
            return false;
        }

        int sep = stored.indexOf(SEPARATOR);
        if (sep <= 0 || sep >= stored.length() - 1) {
            // Not a valid "salt:hash" value (e.g. a leftover plain-text password).
            return false;
        }

        byte[] salt;
        byte[] expectedHash;
        try {
            salt = Base64.decode(stored.substring(0, sep), Base64.NO_WRAP);
            expectedHash = Base64.decode(stored.substring(sep + 1), Base64.NO_WRAP);
        } catch (IllegalArgumentException e) {
            return false;
        }

        byte[] actualHash = pbkdf2(plainPassword.toCharArray(), salt);
        return constantTimeEquals(expectedHash, actualHash);
    }

    /**
     * Derives a key from the password and salt using PBKDF2WithHmacSHA256.
     */
    private static byte[] pbkdf2(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // PBKDF2WithHmacSHA256 is part of the Android platform, so this
            // should never happen; surface it loudly if it ever does.
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Compares two byte arrays without short-circuiting, to avoid leaking the
     * length of the matching prefix through timing.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            // Still run a dummy compare so the branch above doesn't dominate.
            byte[] dummy = Arrays.copyOf(a, b.length);
            int diff = 0;
            for (int i = 0; i < b.length; i++) {
                diff |= dummy[i] ^ b[i];
            }
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
