package com.analyfy.analify;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "javajee123";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Plain: " + rawPassword);
        System.out.println("Hash:  " + encodedPassword);
    }
}