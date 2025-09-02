package com.online_library_service.config;

import com.online_library_service.security.JwtFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring Security Filter Chain...");

        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                // Swagger & Docs
                auth.requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/actuator/health",
                        "/actuator/info"
                ).permitAll();

                // Static file serving
                auth.requestMatchers("/uploads/**").permitAll();

                // Auth APIs
                auth.requestMatchers("/auth/**", "/error").permitAll();

                // Books
                auth.requestMatchers(HttpMethod.GET, "/books", "/books/**").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/books").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT, "/books/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN");

                // Users
                auth.requestMatchers("/users/**").hasAnyRole("ADMIN", "USER");
                auth.requestMatchers("/users/*/borrow/*").hasAnyRole("ADMIN", "USER");
                auth.requestMatchers("/users/*/return/*").hasAnyRole("ADMIN", "USER");

                // Reports
                auth.requestMatchers("/reports/**").hasRole("ADMIN");

                // ✅ Kafka Email Testing Endpoints
                auth.requestMatchers("/api/notifications/**").permitAll();
                // ✅ Allow WebSocket handshake without JWTFilter
                auth.requestMatchers("/ws/**").permitAll();

                // Everything else -> Secure
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
        	    "http://localhost:5173",
        	    "http://10.199.135.114:5173",
        	    "ws://10.199.135.114:8081",   // ✅ Add WebSocket origin
        	    "ws://localhost:8081"         // ✅ Local dev WebSocket
        	));


        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("Initializing BCryptPasswordEncoder bean");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        log.debug("Loading AuthenticationManager from AuthenticationConfiguration");
        return authenticationConfiguration.getAuthenticationManager();
    }
}
