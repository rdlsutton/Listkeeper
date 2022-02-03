package com.radlsuttonedmn.listkeeper;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class to provide data encryption
 */

class DataEncrypter {

    private static SecretKeySpec mEncryptKey;
    private static byte[] mInitVector;
    private static byte[] mSalt;
    private final AppActivityState mAppState;

    // Default constructor
    DataEncrypter() {
        mAppState = new AppActivityState();
    }

    // Initialize a new data encrypter
    void createNew() {
        generateSalt();
        generateInitVector();
    }

    // Validate the entered password
    boolean validPassword() {

        final int MIN_LEN = 8;
        final int MAX_LEN = 16;
        int digits = 0;
        int upChars = 0;
        int loChars = 0;
        int specialChars = 0;

        String password = mAppState.getPassword();

        // Verify that the password meets the minimum and maximum length requirements
        if (password.length() < MIN_LEN || password.length() > MAX_LEN) {
            return false;
        }

        // Verify that the password contains at least one numeric char, at least one upper case char, at least
        // one lower case char and at least one special char
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isDigit(c)) { digits++; }
            if (Character.isUpperCase(c)) { upChars++; }
            if (Character.isLowerCase(c)) { loChars++; }
            if (c >= 33 && c <= 46 || c == 64) { specialChars++; }
        }
        return (digits > 0 && upChars > 0 && loChars > 0 && specialChars > 0);
    }

    // Getters to retrieval of the salt and initialization vector
    String getSalt() {
        return android.util.Base64.encodeToString(mSalt, android.util.Base64.DEFAULT);
    }

    String getInitVector() {
        return android.util.Base64.encodeToString(mInitVector, android.util.Base64.DEFAULT);
    }

    // Setters to allow storage of the salt and initialization vector
    void setSalt(String inSalt) {
        mSalt = android.util.Base64.decode(inSalt, android.util.Base64.DEFAULT);
    }

    void setInitVector(String inIV) {
        mInitVector = android.util.Base64.decode(inIV, android.util.Base64.DEFAULT);
    }

    // Generate a salt array to use in encrypting the data
    private void generateSalt() {

        Random random = new Random();
        mSalt = new byte[8];
        random.nextBytes(mSalt);
    }

    // Generate an initialization vector to use in encrypting the data
    private void generateInitVector() {

        Random random = new Random();
        mInitVector = new byte[128 / 8];
        random.nextBytes(mInitVector);
    }

    // Generate a key spec to use in encrypting and decrypting the data
    ResultType generateEncryptKey() {

        try {
            String password = mAppState.getPassword();
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), mSalt, 10000, 128);
            SecretKey tmp = factory.generateSecret(spec);
            mEncryptKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (GeneralSecurityException e) {
            return ResultType.DECRYPT_FAIL;
        }
        return ResultType.VALID;
    }

    // Encrypt the data
    String encrypt(String stringToEncrypt) {

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, mEncryptKey, new IvParameterSpec(mInitVector));
            return android.util.Base64.encodeToString(cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8)),
                    android.util.Base64.DEFAULT);
        } catch (Exception e) {
            return null;
        }
    }

    // Decrypt the data
    String decrypt(String stringToDecrypt) {

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, mEncryptKey, new IvParameterSpec(mInitVector));
            return new String(cipher.doFinal(android.util.Base64.decode(stringToDecrypt, android.util.Base64.DEFAULT)));
        } catch (Exception e) {
            return null;
        }
    }
}
