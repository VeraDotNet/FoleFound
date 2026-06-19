# GUIDE DE CORRECTIONS - PROBLÈMES CRITIQUES
## Étapes pratiques pour corriger les 5 blocages identifiés

---

## 🔴 CRITIQUE #1: Ambiguïté Constructeurs JWTService

### Fichiers à modifier:
- `src/main/java/com/veradotnet/folefound/users/domain/service/JWTService.java`
- `src/main/java/com/veradotnet/folefound/shared/config/SecurityConfig.java`

### Étape 1: Créer une classe JWTKeyProvider pour gérer la clé
**Nouveau fichier:** `src/main/java/com/veradotnet/folefound/users/domain/service/JWTKeyProvider.java`

```java
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
```

### Étape 2: Modifier JWTService pour utiliser le provider
**Fichier:** `JWTService.java`

```java
package com.veradotnet.folefound.users.domain.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {
    
    private final JWTKeyProvider keyProvider;
    private String secretKey;

    // Initialiser la clé au démarrage du service
    private void init() {
        if (this.secretKey == null) {
            this.secretKey = keyProvider.getOrGenerateSecret();
        }
    }

    public String generateToken(String username) {
        init();  // S'assurer que la clé est chargée
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        init();
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        init();
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        init();
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
```

### Étape 3: Ajouter la configuration JWT en application.yaml

**Fichier:** `src/main/resources/application.yaml`

```yaml
server:
  port: 8080

spring:
  application:
    name: lost-found-api

  datasource:
    url: jdbc:postgresql://localhost:5432/folefound_db
    username: folefounduser
    password: folefoundpass
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true

# ✓ AJOUTER CETTE SECTION
jwt:
  secret: "your-secret-key-minimum-256-bits-long-change-this-in-production-make-it-very-long-and-random-abcdefghijklmnopqrstuvwxyz1234567890"
  expiration: 1800000  # 30 minutes en millisecondes
```

**⚠️ IMPORTANT en production:** Générer une vraie clé secrète!

```bash
# Générer une clé secrète en ligne de commande:
# Linux/Mac:
openssl rand -base64 128

# Windows (PowerShell):
[Convert]::ToBase64String((1..128 | ForEach-Object {[byte](Get-Random -Maximum 256)}))
```

Puis stocker dans `application-prod.yaml` ou variable d'environnement.

### Étape 4: (Optionnel) Créer un @Bean dans SecurityConfig pour plus de clarté

**Fichier:** `SecurityConfig.java` (ajouter la méthode)

```java
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

---

## 🔴 CRITIQUE #2: Clé Secrète Persistée ✓

**C'est déjà résolu par les étapes ci-dessus!**

La clé secrète est maintenant chargée depuis `application.yaml` et persistée.

---

## 🔴 CRITIQUE #3: Exceptions JWT Non Gérées en JWTFilter

### Fichier à modifier:
- `src/main/java/com/veradotnet/folefound/users/application/filter/JWTFilter.java`

**Code corrigé:**

```java
package com.veradotnet.folefound.users.application.filter;

import com.veradotnet.folefound.users.domain.service.JWTService;
import com.veradotnet.folefound.users.domain.service.MyUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;  // ✓ Injecter directement
    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        String autHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        try {
            // Étape 1: Extraire le token du header
            if (autHeader != null && autHeader.startsWith("Bearer ")) {
                token = autHeader.substring(7);
                
                try {
                    // Étape 2: Extraire le username du JWT
                    username = jwtService.extractUsername(token);
                } catch (JwtException e) {
                    logger.debug("JWT parsing failed: {}", e.getMessage());
                    // Token invalide, continuer sans authentifier
                    username = null;
                }
            }

            // Étape 3: Si username trouvé et pas déjà authentifié
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    // Étape 4: Charger les détails utilisateur
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // Étape 5: Valider le token
                    if (jwtService.validateToken(token, userDetails)) {
                        // Étape 6: Créer le token d'authentification
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, 
                                    null, 
                                    userDetails.getAuthorities()
                                );
                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                        );

                        // Étape 7: Stocker dans le SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("JWT validated successfully for user: {}", username);
                    } else {
                        logger.debug("JWT validation failed for user: {}", username);
                    }
                } catch (UsernameNotFoundException e) {
                    logger.debug("User not found: {}", username);
                    // Utilisateur n'existe pas, continuer sans authentification
                } catch (JwtException e) {
                    logger.debug("JWT validation error: {}", e.getMessage());
                    // Token expiré ou signature invalide
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in JWTFilter", e);
            // Continuer le filtre même en cas d'erreur inattendue
        }

        // Continuer la chaîne de filtres en toute circonstance
        filterChain.doFilter(request, response);
    }
}
```

**Changements clés:**
- ✓ Wrap `extractUsername()` dans try-catch pour JwtException
- ✓ Wrap `loadUserByUsername()` dans try-catch pour UsernameNotFoundException
- ✓ Injecter `MyUserDetailsService` directement au lieu de `context.getBean()`
- ✓ Ajouter logging des erreurs
- ✓ Continuer le filtre en toute circonstance (erreur = pas d'authentification)

---

## 🔴 CRITIQUE #4: Username Sans UNIQUE Constraint

### Fichier à modifier:
- `src/main/java/com/veradotnet/folefound/users/domain/model/Users.java`

**Code corrigé:**

```java
package com.veradotnet.folefound.users.domain.model;

import com.veradotnet.folefound.users.application.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)  // ✓ Ajouter unique = true
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String studentCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)  // ✓ Ajouter nullable = false
    private Boolean isActive = true;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModified;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "userProfile_id")
    private UserProfile userProfile;
}
```

**Changements:**
- ✓ Ajouter `unique = true` sur username
- ✓ Ajouter `nullable = false` sur isActive (optionnel mais mieux)

### Migration SQL (si vous utilisez Flyway/Liquibase)

**Nouveau fichier:** `src/main/resources/db/migration/V1__Add_Username_Unique_Constraint.sql`

```sql
-- Ajouter la contrainte UNIQUE sur username
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE(username);

-- Créer un index pour améliorer les performances
CREATE INDEX idx_users_username ON users(username);
```

**Ou si la table existe déjà et a potentiellement des doublons:**

```sql
-- Vérifier les usernames dupliqués avant d'appliquer la contrainte
SELECT username, COUNT(*) FROM users GROUP BY username HAVING COUNT(*) > 1;

-- Si doublons trouvés, les nettoyer manuellement

-- Puis appliquer la contrainte
ALTER TABLE users ADD CONSTRAINT uk_users_username UNIQUE(username);
CREATE INDEX idx_users_username ON users(username);
```

---

## 🔴 CRITIQUE #5: Exception UsernameNotFoundException Non Gérée

### Fichiers à modifier:
1. `src/main/java/com/veradotnet/folefound/users/application/filter/JWTFilter.java` - Déjà corrigé dans CRITIQUE #3 ✓

2. Créer un `@ControllerAdvice` pour centraliser les exceptions

**Nouveau fichier:** `src/main/java/com/veradotnet/folefound/shared/exception/GlobalExceptionHandler.java`

```java
package com.veradotnet.folefound.shared.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les exceptions IllegalArgumentException
     * (username exists, student code exists, incorrect credentials)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Gère les ressources non trouvées
     */
    @ExceptionHandler(RessourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            RessourceNotFoundException ex,
            WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Gère les utilisateurs non trouvés
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(
            UsernameNotFoundException ex,
            WebRequest request) {
        
        logger.debug("User not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Invalid credentials")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Gère les erreurs de validation Bean
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");
        
        logger.debug("Validation error: {}", message);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Gère les erreurs non anticipées
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        logger.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal server error")
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

**Nouveau fichier:** `src/main/java/com/veradotnet/folefound/shared/exception/ErrorResponse.java`

```java
package com.veradotnet.folefound.shared.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    
    @JsonProperty("status")
    private int status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("path")
    private String path;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
}
```

---

## ✅ Résumé des Fichiers à Modifier/Créer

### À MODIFIER (Existants):
1. ✏️ `JWTService.java` - Supprimer constructeur par défaut
2. ✏️ `JWTFilter.java` - Ajouter gestion exceptions, injecter MyUserDetailsService
3. ✏️ `Users.java` - Ajouter `unique = true` sur username
4. ✏️ `application.yaml` - Ajouter jwt.secret et jwt.expiration

### À CRÉER (Nouveaux):
1. ➕ `JWTKeyProvider.java` - Gérer la clé secrète
2. ➕ `GlobalExceptionHandler.java` - Centraliser exceptions
3. ➕ `ErrorResponse.java` - DTO pour erreurs
4. ➕ `V1__Add_Username_Unique_Constraint.sql` - Migration DB

---

## 🧪 Test Rapide des Corrections

### Tester l'Inscription
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!",
    "studentCode": "STU12345",
    "userProfileDTO": {
      "firstName": "Test",
      "lastName": "User",
      "email": "test@university.edu"
    }
  }'

# Expected: 201 CREATED
```

### Tester le Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "TestPass123!"
  }'

# Expected: 200 OK + JWT Token
# Example: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Tester l'Accès Protégé
```bash
curl -X GET http://localhost:8080/api/v1/campuses \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Expected: 200 OK + Campus list
```

### Tester avec Token Invalide
```bash
curl -X GET http://localhost:8080/api/v1/campuses \
  -H "Authorization: Bearer invalid.token.here"

# Expected: 401 Unauthorized (pas de crash!)
```

### Tester Username Dupliqué
```bash
# Créer un utilisateur
curl -X POST http://localhost:8080/api/v1/auth/register ...

# Essayer de créer le même username
curl -X POST http://localhost:8080/api/v1/auth/register ...

# Expected: 409 Conflict (pas 500!)
```

---

## 📋 Checklist de Déploiement

- [ ] Ajouter `JWTKeyProvider.java`
- [ ] Modifier `JWTService.java`
- [ ] Modifier `JWTFilter.java`
- [ ] Modifier `Users.java`
- [ ] Créer `GlobalExceptionHandler.java`
- [ ] Créer `ErrorResponse.java`
- [ ] Ajouter `jwt.secret` en `application.yaml`
- [ ] Générer et configurer une vraie clé secrète en prod
- [ ] Exécuter migration SQL pour ajouter UNIQUE constraint
- [ ] Compiler et tester localement
- [ ] Exécuter tous les tests unitaires
- [ ] Faire un redémarrage du serveur et vérifier JWT toujours valide
- [ ] Tester les 5 scénarios ci-dessus
- [ ] Déployer en prod avec nouvelle clé secrète

---

**Temps estimé de mise en œuvre:** 2-3 heures  
**Risque si non fait:** Application non fonctionnelle, sécurité compromise
