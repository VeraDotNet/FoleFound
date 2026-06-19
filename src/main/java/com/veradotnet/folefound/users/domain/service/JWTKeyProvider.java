package com.veradotnet.folefound.users.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class JWTKeyProvider {
    
    @Value("${jwt.secret:#{null}}")
    private String configuredSecret;

    public String getOrGenerateSecret() {
        // Si secret est configuré, l'utiliser
        if (configuredSecret != null && !configuredSecret.isEmpty()) {
            return configuredSecret;
        }
        // Sinon, générer une clé (mais attention: sera perdue au redémarrage!)
        return generateNewSecret();
    }

    private String generateNewSecret() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(sk.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate JWT secret key", e);
        }
    }
}