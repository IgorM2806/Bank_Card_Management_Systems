package org.example.service;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void  main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        String secretKey = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Случайный секретный ключ: " + secretKey);
    }
}
