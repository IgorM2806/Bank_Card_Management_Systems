package org.example.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("temp123");
        System.out.println(hash);
    }
}
//testpass - user='999'
//temp - user='8'
//user123 - user='1'
//admin123 - admin='6'
//admin123456 - admin='7'
//temp - user='12'