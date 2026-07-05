package com.veradotnet.folefound.shared.utils;

import com.veradotnet.folefound.users.domain.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    /**
     * Extrait l'ID de l'utilisateur actuellement connecté au système
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Aucun utilisateur authentifié trouvé dans le contexte de sécurité.");
        }

        Object principal = authentication.getPrincipal();

        // Cas standard 2 : Si tu as créé ton propre "UserPrincipal" dans ton module Auth
        if (principal instanceof UserPrincipal userPrincipal) {
             return userPrincipal.getId();
        }

        throw new IllegalArgumentException("Le type de Principal d'authentification n'est pas supporté.");
    }
}
