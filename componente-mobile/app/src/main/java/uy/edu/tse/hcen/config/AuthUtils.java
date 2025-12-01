package uy.edu.tse.hcen.config;

import android.util.Base64;

import java.security.SecureRandom;

public class AuthUtils {

    private static final String ALPHANUM = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RAND = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RAND.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}

