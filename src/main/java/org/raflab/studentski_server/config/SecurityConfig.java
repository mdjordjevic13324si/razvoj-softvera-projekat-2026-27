package org.raflab.studentski_server.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.raflab.studentski_server.model.Uloga;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/*
 * Autentifikacija preko JWT tokena (Authorization: Bearer <token>), bez sesije.
 * Token se dobija na POST /api/auth/login, a uloga iz tokena (STUDENT/NASTAVNIK) postaje ROLE_STUDENT/ROLE_NASTAVNIK.
 *
 * Grubo pravilo po URL-u je ovde, a provera "student vidi samo svoje podatke" je u @PreAuthorize + PristupService.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    public static final String CLAIM_ULOGA = "uloga";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)   // nema cookie sesije, pa ni CSRF rizika
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
                        // zadatke i spiskove za polaganje vidi samo nastavnik
                        // (student zadatak dobija tek preuzimanjem u toku termina - stavka 4)
                        .requestMatchers("/api/zadaci/**", "/api/grupe/*/spisak/**").hasRole(Uloga.NASTAVNIK.name())
                        // monitoring (ko je preuzeo, ko je predao) - samo nastavnik
                        .requestMatchers("/api/monitoring/**").hasRole(Uloga.NASTAVNIK.name())
                        // pregled i ocenjivanje - samo nastavnik; izuzetak je jedno resenje (poeni i komentari),
                        // koje sme da vidi i student, ali samo svoje (provera vlasnistva je u PristupService)
                        .requestMatchers(HttpMethod.GET, "/api/resenja/zip").hasRole(Uloga.NASTAVNIK.name())
                        .requestMatchers(HttpMethod.GET, "/api/resenja/*").authenticated()
                        .requestMatchers("/api/resenja/**").hasRole(Uloga.NASTAVNIK.name())
                        // polaganje (pregled, preuzimanje zadatka, kasnije predaja) - samo student, i to samo svoje;
                        // ovo je izuzetak od pravila "POST sme samo nastavnik"
                        .requestMatchers("/api/polaganja/**").hasRole(Uloga.STUDENT.name())
                        // licne podatke svih studenata (ime, email, indeks) vidi samo nastavnik
                        .requestMatchers("/api/studenti/**").hasRole(Uloga.NASTAVNIK.name())
                        // genericki CRUD: citaju svi prijavljeni...
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        // ...sve ostalo (POST/PUT/DELETE) samo nastavnik.
                        // Izuzeci gde student sme da pise (npr. prijava ispita) dodaju se IZNAD ovih pravila.
                        .anyRequest().hasRole(Uloga.NASTAVNIK.name()))
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(CLAIM_ULOGA);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public SecretKey jwtKey(SecurityProperties props) {
        byte[] secret = props.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("app.security.jwt-secret mora imati najmanje 32 bajta");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtKey) {
        return NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    // cuva lozinke kao {bcrypt}$2a$...
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties props) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(props.corsOrigins());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}
