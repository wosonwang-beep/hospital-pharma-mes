package com.hospital.mes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.hospital.mes", exclude = UserDetailsServiceAutoConfiguration.class)
public class MesApplication {
    public static void main(String[] args) { SpringApplication.run(MesApplication.class, args); }
}
