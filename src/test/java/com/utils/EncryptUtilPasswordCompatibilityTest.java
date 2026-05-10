package com.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EncryptUtilPasswordCompatibilityTest {

    @Test
    void normalizeIncomingPasswordShouldDecryptAesCiphertext() {
        String ciphertext = EncryptUtil.aesEncrypt("movie-pass");

        assertEquals("movie-pass", EncryptUtil.normalizeIncomingPassword(ciphertext));
    }

    @Test
    void normalizeIncomingPasswordShouldFallbackToOriginalPlaintext() {
        assertEquals("movie-pass", EncryptUtil.normalizeIncomingPassword("movie-pass"));
    }

    @Test
    void encryptPasswordForStorageShouldAlwaysReturnCiphertext() {
        String stored = EncryptUtil.encryptPasswordForStorage(EncryptUtil.aesEncrypt("movie-pass"));

        assertEquals(EncryptUtil.aesEncrypt("movie-pass"), stored);
        assertNotEquals("movie-pass", stored);
    }

    @Test
    void passwordMatchesShouldAcceptLegacyPlaintextStorage() {
        assertTrue(EncryptUtil.passwordMatches(EncryptUtil.aesEncrypt("movie-pass"), "movie-pass"));
    }

    @Test
    void passwordMatchesShouldAcceptEncryptedStorage() {
        assertTrue(EncryptUtil.passwordMatches(EncryptUtil.aesEncrypt("movie-pass"), EncryptUtil.aesEncrypt("movie-pass")));
    }

    @Test
    void passwordMatchesShouldRejectWrongPassword() {
        assertFalse(EncryptUtil.passwordMatches(EncryptUtil.aesEncrypt("wrong-pass"), EncryptUtil.aesEncrypt("movie-pass")));
    }
}
