package com.hospital.mes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hospital.mes")
public class MesApplication {
    public static void main(String[] args) { SpringApplication.run(MesApplication.class, args); }
}
