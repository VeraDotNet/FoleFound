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
import org.springframework.context.ApplicationContext;
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

    private final MyUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // On demande au filtre de ne PAS s'appliquer sur les routes d'authentification et de Swagger
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Bearer eyghbjndkpoo57.cijepawl[lp[clcnomeomocmlmdnkdnvoioewlmcknal.vnijvisdovkpokwooqaywwshcncnieniwopa
        String autHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        try{
        
            if(autHeader != null && autHeader.startsWith("Bearer ")){
                token = autHeader.substring(7);
                try {
                    //Extraire le username du JWT
                    username = jwtService.extractUsername(token);
                } catch (JwtException e) {
                    logger.debug("JWT parsing failed: {}");
                    // Laisser le filtre continuer - l'endpoint @Secured rejettera la requête
                }
            }

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

                try{
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.validateToken(token, userDetails)){
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource()
                                                .buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (UsernameNotFoundException e) {
                    logger.debug("User not found: " + e.getMessage());
                    // Laisser le filtre continuer - l'endpoint @Secured rejettera la requête
                } catch (io.jsonwebtoken.JwtException e){
                    // Token JWT invalide, expiré, mal formé, etc.
                    logger.debug("JWT validation failed: " + e.getMessage());
                    // Laisser le filtre continuer - l'endpoint @Secured rejettera la requête
                } 
            }
        }catch (Exception e) {
            logger.error("Unexpected error", e);
        }

        filterChain.doFilter(request, response);
    }
}
    