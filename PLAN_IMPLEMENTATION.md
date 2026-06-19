# PLAN D'IMPLÉMENTATION DÉTAILLÉ

## Phase 1: Corrections Critiques (Jour 1)

### Jour 1 - Matin (4 heures)

#### Tâche 1.1: Corriger l'ambiguïté JWTService (45 min)
**Responsable:** Développeur Senior  
**Fichiers:** 
- JWTService.java (modifier)
- JWTKeyProvider.java (créer)

**Actions:**
1. [ ] Créer `JWTKeyProvider.java` 
2. [ ] Modifier `JWTService.java` - supprimer constructeur par défaut
3. [ ] Ajouter l'injection de `JWTKeyProvider` dans `JWTService`
4. [ ] Compiler et vérifier pas d'erreurs
5. [ ] Tester le démarrage du service

**Vérification:** Application démarre sans erreur

```bash
mvn clean compile
mvn spring-boot:run
# Doit démarrer sans exception JWTService
```

---

#### Tâche 1.2: Configurer la clé secrète persistée (30 min)
**Responsable:** Développeur Junior  
**Fichiers:** application.yaml

**Actions:**
1. [ ] Ajouter section `jwt:` dans `application.yaml`
2. [ ] Générer une clé secrète sécurisée (voir commande ci-dessous)
3. [ ] Ajouter `jwt.secret` avec la clé
4. [ ] Ajouter `jwt.expiration: 1800000`
5. [ ] Redémarrer et vérifier la clé est chargée

**Génération de clé secrète:**
```bash
# Linux/Mac:
openssl rand -base64 128

# Windows (PowerShell):
[Convert]::ToBase64String((1..128 | ForEach-Object {[byte](Get-Random -Maximum 256)}))
```

---

#### Tâche 1.3: Corriger JWTFilter (1 heure)
**Responsable:** Développeur Senior  
**Fichiers:** JWTFilter.java

**Actions:**
1. [ ] Remplacer `context.getBean()` par injection directe de `MyUserDetailsService`
2. [ ] Ajouter try-catch autour de `extractUsername()`
3. [ ] Ajouter try-catch autour de `loadUserByUsername()`
4. [ ] Ajouter logging avec SLF4J
5. [ ] Compiler et vérifier
6. [ ] Tester avec token invalide (ne doit pas crasher)

**Test:**
```bash
curl -X GET http://localhost:8080/api/v1/campuses \
  -H "Authorization: Bearer invalid.token"
# Doit retourner 401 ou laisser continuer (security reject)
# PAS de 500!
```

---

#### Tâche 1.4: Ajouter contrainte UNIQUE sur username (45 min)
**Responsable:** Développeur Junior  
**Fichiers:** 
- Users.java (modifier)
- Migration SQL (créer)

**Actions:**
1. [ ] Modifier `Users.java` - ajouter `unique = true` sur username
2. [ ] Créer fichier migration SQL `V1__Add_Username_Unique_Constraint.sql`
3. [ ] Compiler et vérifier pas d'erreurs
4. [ ] Exécuter migration (Hibernate créera le constraint)

**Vérification:**
```sql
-- Se connecter à PostgreSQL et vérifier le constraint
\d users
-- Doit voir: "Constraints: ... uk_users_username UNIQUE(username)"
```

---

#### Tâche 1.5: Créer GlobalExceptionHandler (1 heure)
**Responsable:** Développeur Senior  
**Fichiers:**
- GlobalExceptionHandler.java (créer)
- ErrorResponse.java (créer)

**Actions:**
1. [ ] Créer `GlobalExceptionHandler.java` avec @ControllerAdvice
2. [ ] Créer `ErrorResponse.java` DTO
3. [ ] Implémenter les 5 handlers d'exceptions
4. [ ] Compiler et vérifier
5. [ ] Tester chaque cas d'erreur

**Test des erreurs:**
```bash
# Erreur: Username existant
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "alice", ...}'
# Doit retourner 409 Conflict (pas 500)

# Erreur: Ressource non trouvée
curl -X GET http://localhost:8080/api/v1/campuses/9999
# Doit retourner 404 Not Found (pas 500)
```

---

### Jour 1 - Après-midi (Test & Validation - 3 heures)

#### Tâche 1.6: Test d'intégration complet (1.5 heures)
**Responsable:** Testeur  
**Outils:** Postman ou curl

**Scénarios de test:**

1. **Test Inscription (Success)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "newuser",
       "password": "Password123!",
       "studentCode": "STU99999",
       "userProfileDTO": {
         "firstName": "New",
         "lastName": "User",
         "email": "newuser@test.edu"
       }
     }'
   # Attendu: 201 CREATED
   ```

2. **Test Inscription (Username Existant)**
   ```bash
   # Première inscription réussit, ensuite:
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "newuser", ...}'
   # Attendu: 409 Conflict (pas 500 Internal Error)
   ```

3. **Test Login (Success)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "newuser", "password": "Password123!"}'
   # Attendu: 200 OK + JWT Token
   ```

4. **Test Login (Incorrect Password)**
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "newuser", "password": "wrongpassword"}'
   # Attendu: 401 Unauthorized (pas 500)
   ```

5. **Test JWT Valide**
   ```bash
   # Récupérer le JWT du login #3
   curl -X GET http://localhost:8080/api/v1/campuses \
     -H "Authorization: Bearer <JWT_TOKEN>"
   # Attendu: 200 OK + Campus list
   ```

6. **Test JWT Invalide**
   ```bash
   curl -X GET http://localhost:8080/api/v1/campuses \
     -H "Authorization: Bearer invalid.fake.token"
   # Attendu: 401 Unauthorized (pas 500 ni crash)
   ```

7. **Test JWT Expiré**
   ```bash
   # Attendre 31 minutes ou modifier JWTService.generateToken() 
   # temporairement pour expiration=1000ms (1 sec) pour test
   curl -X GET http://localhost:8080/api/v1/campuses \
     -H "Authorization: Bearer <EXPIRED_JWT>"
   # Attendu: 401 Unauthorized
   ```

---

#### Tâche 1.7: Redémarrage du serveur & Validation JWT (30 min)
**Responsable:** DevOps/Développeur

**Test:** Les JWTs restent valides après redémarrage

1. [ ] Générer JWT avec serveur running
2. [ ] Copier le token
3. [ ] Arrêter le serveur (`Ctrl+C`)
4. [ ] Redémarrer le serveur (`mvn spring-boot:run`)
5. [ ] Envoyer requête avec le JWT précédent
6. [ ] Vérifier que ça fonctionne (pas 500 signature error)

```bash
# T1: Login et récupérer JWT
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "Password123!"}' | jq -r '.')

echo "Token: $TOKEN"

# T2: Arrêter et redémarrer le serveur
# Ctrl+C et mvn spring-boot:run

# T3: Utiliser le token
curl -X GET http://localhost:8080/api/v1/campuses \
  -H "Authorization: Bearer $TOKEN"
# Doit retourner 200 OK!
```

---

#### Tâche 1.8: Documentation et Handoff (1 heure)
**Responsable:** Développeur Senior + Tech Lead

1. [ ] Mettre à jour le README.md avec la clé JWT
2. [ ] Documenter les changements en CHANGELOG.md
3. [ ] Créer une checklist pour la production
4. [ ] Préparer le script de génération de clé

---

## Phase 2: Corrections Importants (Jours 2-3)

### Tâche 2.1: Corriger Boolean isActive (1 heure)
**Responsable:** Développeur Junior  
**Fichiers:** Users.java, UserPrincipal.java

```java
// Users.java
private boolean isActive = true;  // ← boolean primitive, pas Boolean

// UserPrincipal.java
@Override
public boolean isAccountNonExpired() {
    return true;  // ← Toujours true pour now
}

@Override
public boolean isEnabled() {
    return user.getIsActive();  // ← Nouveau, check isActive
}
```

---

### Tâche 2.2: Consolider BCryptPasswordEncoder (1 heure)
**Responsable:** Développeur Junior  
**Fichiers:** SecurityConfig.java, UserService.java

```java
// SecurityConfig.java - Créer @Bean
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder(12);
}

// UserService.java - Injecter
@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder encoder;  // ← Injecter au lieu de créer
    // ...
}
```

---

### Tâche 2.3: Ajouter Logging d'Authentification (1 heure)
**Responsable:** Développeur Junior  
**Fichiers:** UserService.java

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public UserDTO register(UserDTO userDTO) {
        // ... validation ...
        logger.info("User registered: username={}, studentCode={}", 
                   userDTO.getUsername(), userDTO.getStudentCode());
        // ...
    }
    
    public String verify(LoginRequest loginRequest) {
        try {
            // ...
            logger.info("Login successful: username={}", loginRequest.getUsername());
        } catch (Exception e) {
            logger.warn("Login failed: username={}, reason={}", 
                       loginRequest.getUsername(), e.getMessage());
        }
    }
}
```

---

### Tâche 2.4: Implémenter Rate Limiting (2 heures)
**Responsable:** Développeur Senior  
**Dépendance:** Bucket4j

1. [ ] Ajouter `bucket4j-core` en pom.xml
2. [ ] Créer `RateLimitFilter.java`
3. [ ] Enregistrer le filtre dans `SecurityConfig.java`
4. [ ] Tester avec 15 requêtes/min limite

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

---

## Phase 3: Corrections Mineurs (Jour 4)

### Tâche 3.1: Validation Password Fort (30 min)
### Tâche 3.2: Ajouter Pagination (1 heure)
### Tâche 3.3: Configurer CORS (30 min)
### Tâche 3.4: Ajouter JWT Claims (1 heure)

---

## Dépendances de Tâches

```
Tâche 1.1 ─┬─→ Compilation OK
           │
Tâche 1.2 ─┼─→ Config JWT
           │
Tâche 1.3 ─┼─→ JWTFilter robuste
           │
Tâche 1.4 ─┼─→ DB constraint
           │
Tâche 1.5 ─┴─→ Exception handling
               │
               ├─→ Tâche 1.6: Tests d'intégration
               │             (Dépend de tout 1.1-1.5)
               │
               ├─→ Tâche 1.7: Redémarrage test
               │
               └─→ Tâche 1.8: Documentation
```

---

## Métriques de Succès

| Métrique | Critère | Vérification |
|----------|---------|--------------|
| **Démarrage** | App démarre sans erreur | `mvn spring-boot:run` |
| **JWT Persistence** | Token valide après redémarrage | Test 1.7 |
| **Exception Handling** | 409 sur username dupliqué | Test 1.6 #2 |
| **JWT Validation** | 401 sur token invalide | Test 1.6 #6 |
| **Password Hashing** | Passwords différents en BD | Vérifier la BD |
| **Logs** | Logins tracés | Voir console/logs |
| **Unicité Username** | Constraint créé | `\d users` en psql |

---

## Rollback Plan

Si quelque chose va mal:

1. **Backup BD avant migration:**
   ```bash
   pg_dump -U folefounduser folefound_db > backup_before_fixes.sql
   ```

2. **Revert des fichiers Java:**
   ```bash
   git checkout -- src/
   ```

3. **Revert la migration (si Flyway):**
   ```bash
   # Supprimer la dernière migration de src/main/resources/db/migration/
   ```

4. **Redémarrer:**
   ```bash
   mvn clean spring-boot:run
   ```

---

## Timeline Estimée

| Phase | Durée | Cumulative |
|-------|-------|-----------|
| Phase 1 (Critiques) | 7 heures | 7h |
| Phase 2 (Importants) | 5 heures | 12h |
| Phase 3 (Mineurs) | 4 heures | 16h |
| **Total** | **16 heures** | **16h** |

**Effort d'équipe recommandé:**
- 2-3 développeurs travaillant en parallèle
- 1 QA pour les tests
- 1 DevOps pour la BD et déploiement

**Timeline réaliste:** 2-3 jours avec équipe complète

