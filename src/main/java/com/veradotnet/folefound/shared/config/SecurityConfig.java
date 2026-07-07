package com.veradotnet.folefound.shared.config;

import com.veradotnet.folefound.users.application.filter.JWTFilter;
import com.veradotnet.folefound.users.domain.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // Injection by constructor
    private final MyUserDetailsService myUserDetailsService;

    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configure(http))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error")
                        .permitAll()

                        // Lecture (GET) access to everyone
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/campus/**").hasAnyRole("ADMIN", "AGENT", "STUDENT", "PERSONNEL")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/location/**").hasAnyRole("ADMIN", "AGENT", "STUDENT", "PERSONNEL")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/category/**").hasAnyRole("ADMIN", "AGENT", "STUDENT", "PERSONNEL")

                        //Ecriture réservée a ladmin/agent
                        .requestMatchers(
                                "/api/v1/campus/**").hasAnyRole("ADMIN")
                        .requestMatchers(
                                "/api/v1/location/**").hasAnyRole("ADMIN")
                        .requestMatchers(
                                "/api/v1/category/**").hasAnyRole("ADMIN", "AGENT")

                        //Administration only
                        .requestMatchers(
                                "/api/v1/admin/user/**").hasAnyRole("ADMIN")
                        .requestMatchers(
                                "/api/v1/preRegistration/**").hasAnyRole("ADMIN")
                        .requestMatchers(
                                "/api/v1/declaration/search").hasAnyRole("ADMIN", "AGENT")

                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/declaration/my-declarations").hasAnyRole("STUDENT", "PERSONNEL")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/v1/declaration/archive/{id}").hasAnyRole("AGENT", "STUDENT", "PERSONNEL")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/declaration/{id}").hasAnyRole("AGENT", "STUDENT", "PERSONNEL")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/v1/declaration/{id}").hasAnyRole("AGENT", "STUDENT", "PERSONNEL")

                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/declaration").hasAnyRole("STUDENT","PERSONNEL")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/declaration").hasAnyRole("AGENT")

                        .requestMatchers(
                                "/api/v1/stats/**").hasAnyRole("ADMIN","AGENT")
                        .requestMatchers(
                                "/api/v1/matching/**").hasAnyRole("AGENT")
                        .requestMatchers(
                                "/api/v1/restitution/**").hasAnyRole("AGENT")
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // we can directly link the service here
                //.userDetailsService(myUserDetailsService)
                //go to jwtfilter now before the username and password
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
   
    @Bean
    public AuthenticationProvider authenticationProvider(BCryptPasswordEncoder passwordEncoder){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(myUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);  // Réutiliser le @Bean
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

}
