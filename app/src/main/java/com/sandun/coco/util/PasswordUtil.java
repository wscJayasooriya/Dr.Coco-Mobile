package com.sandun.coco.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean isValidPassword(String password) {
        // Check for length
        if (password.length() < 8) {
            return false;
        }

        // Regular expression to check for at least one uppercase letter
        String upperCasePattern = ".*[A-Z].*";
        // Regular expression to check for at least one lowercase letter
        String lowerCasePattern = ".*[a-z].*";
        // Regular expression to check for at least one digit
        String digitPattern = ".*[0-9].*";
        // Regular expression to check for at least one special character
        String specialCharPattern = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*";

        // Check if password matches all patterns
        if (!password.matches(upperCasePattern)) {
            return false;
        }
        if (!password.matches(lowerCasePattern)) {
            return false;
        }
        if (!password.matches(digitPattern)) {
            return false;
        }
        if (!password.matches(specialCharPattern)) {
            return false;
        }

        return true;
    }
}
