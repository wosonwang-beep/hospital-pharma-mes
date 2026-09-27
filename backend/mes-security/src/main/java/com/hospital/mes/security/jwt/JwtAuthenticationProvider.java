package com.hospital.mes.security.jwt;
import org.springframework.security.core.Authentication;
public interface JwtAuthenticationProvider { Authentication authenticate(String token); }
