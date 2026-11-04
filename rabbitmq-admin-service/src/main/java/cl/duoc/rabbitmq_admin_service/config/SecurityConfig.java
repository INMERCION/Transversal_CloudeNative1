package cl.duoc.rabbitmq_admin_service.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    // =====================================================
    // PROTEGER LOS ENDPOINTS ADMINISTRATIVOS
    // =====================================================

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.admin-scope}") String adminScope)
            throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Solicitudes CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Salud de la aplicación
                        .requestMatchers("/actuator/health")
                        .permitAll()

                        // Administración RabbitMQ
                        
                        .requestMatchers("/api/admin/**")
                        .access((authentication, context) -> {

                            var usuarioAutenticado = authentication.get();

                            boolean tieneScope = usuarioAutenticado.getAuthorities()
                                    .stream()
                                    .anyMatch(a -> a.getAuthority()
                                            .equals("SCOPE_" + adminScope));

                            boolean perteneceGrupo = false;

                            if (usuarioAutenticado instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {

                                java.util.List<String> grupos = jwtAuth.getToken()
                                        .getClaimAsStringList("cognito:groups");

                                perteneceGrupo = grupos != null
                                        && grupos.contains("RabbitMQAdmins");
                            }

                            return new org.springframework.security.authorization.AuthorizationDecision(
                                    tieneScope && perteneceGrupo);
                        })

                        // Bloquear el resto
                        .anyRequest()
                        .denyAll()
                )

                .oauth2ResourceServer(oauth ->
                        oauth.jwt(Customizer.withDefaults()))

                .build();
    }

    // =====================================================
    // VALIDAR TOKEN DE AWS COGNITO
    // =====================================================

    @Bean
    OAuth2TokenValidator<Jwt> cognitoValidator(

            @Value("${app.cognito.issuer}") String issuer,

            @Value("${app.cognito.client-id}") String clientId) {

        return new DelegatingOAuth2TokenValidator<>(

                // Validar emisor y propiedades estándar
                JwtValidators.createDefaultWithIssuer(issuer),

                // Solamente permitir Access Tokens
                new JwtClaimValidator<String>(
                        "token_use",
                        "access"::equals),

                // Comprobar el App Client de Cognito
                new JwtClaimValidator<String>(
                        "client_id",
                        clientId::equals)
        );
    }

    // =====================================================
    // DECODIFICAR Y VALIDAR FIRMA JWT
    // =====================================================

    @Bean
    JwtDecoder jwtDecoder(

            @Value("${app.cognito.issuer}") String issuer,

            OAuth2TokenValidator<Jwt> cognitoValidator) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(
                        issuer + "/.well-known/jwks.json")
                .build();

        decoder.setJwtValidator(cognitoValidator);

        return decoder;
    }

    // =====================================================
    // PERMITIR FRONTEND ANGULAR AUTORIZADO
    // =====================================================

    @Bean
    CorsConfigurationSource corsConfigurationSource(

            @Value("${app.cors.origin}") String origin) {

        CorsConfiguration cors = new CorsConfiguration();

        cors.setAllowedOrigins(List.of(origin));

        cors.setAllowedMethods(
                List.of("GET", "POST", "DELETE", "OPTIONS"));

        cors.setAllowedHeaders(
                List.of("Authorization", "Content-Type"));

        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/admin/**", cors);

        return source;
    }
}
