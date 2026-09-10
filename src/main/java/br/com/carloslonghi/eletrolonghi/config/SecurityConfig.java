package br.com.carloslonghi.eletrolonghi.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    @Value("${spring.security.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/api-docs.yaml").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**" ).permitAll()

                        // Marcas e acessórios: leitura para atendimento; escrita e remoção (soft) para gestão.
                        .requestMatchers(HttpMethod.GET, "/brand", "/brand/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/brand").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/brand/*").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/accessory", "/accessory/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/accessory").hasAnyRole("ADMIN", "GERENTE")
                        .requestMatchers(HttpMethod.DELETE, "/accessory/*").hasAnyRole("ADMIN", "GERENTE")

                        // Clientes e aparelhos: cadastro/edição/consulta pelo atendimento; remoção (soft) pela gestão.
                        .requestMatchers(HttpMethod.GET, "/customer", "/customer/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/customer").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/customer/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/customer/*").hasAnyRole("ADMIN", "GERENTE")
                        // Listagem e busca por número de série ficam com o atendimento; o detalhe de
                        // um aparelho (GET /device/{id}) também é liberado ao TÉCNICO, que chega nele
                        // a partir da ordem de reparo para ver as informações do aparelho.
                        .requestMatchers(HttpMethod.GET, "/device", "/device/serial-number").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.GET, "/device/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE", "TECNICO")
                        .requestMatchers(HttpMethod.POST, "/device").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/device/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/device/*").hasAnyRole("ADMIN", "GERENTE")

                        // Ordens de reparo: o status do serviço só o TÉCNICO (e a gestão) altera;
                        // o atendimento abre/edita a ordem e todos os papéis operacionais consultam.
                        .requestMatchers(HttpMethod.PATCH, "/repair-order/*/status").hasAnyRole("ADMIN", "GERENTE", "TECNICO")
                        .requestMatchers(HttpMethod.GET, "/repair-order", "/repair-order/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE", "TECNICO")
                        .requestMatchers(HttpMethod.POST, "/repair-order").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/repair-order/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/repair-order/*").hasAnyRole("ADMIN", "GERENTE")

                        // Pagamentos: fluxo completo (incl. checkout/sync do Mercado Pago) para o atendimento;
                        // remoção (soft) para a gestão.
                        .requestMatchers(HttpMethod.GET, "/payment", "/payment/*", "/payment/*/receipt").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.POST, "/payment", "/payment/*/checkout", "/payment/*/sync").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PUT, "/payment/*").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.PATCH, "/payment/*/status").hasAnyRole("ADMIN", "GERENTE", "ATENDENTE")
                        .requestMatchers(HttpMethod.DELETE, "/payment/*").hasAnyRole("ADMIN", "GERENTE")

                        // Gestão de usuários: exclusiva do ADMIN.
                        .requestMatchers(HttpMethod.GET, "/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user/*/role", "/user/*/status").hasRole("ADMIN")

                        // Nada de acesso implícito: papéis sem regra explícita (ex.: PENDENTE) são barrados.
                        .anyRequest().denyAll()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Allows the configured front-end origins (web app, Electron renderer, etc.)
     * to call the API from a browser context, including the preflight
     * (OPTIONS) requests that browsers send before the real request.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Ensures unauthenticated/invalid-token requests return a clear 401 JSON
     * response instead of falling back to a generic 403.
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Token de autenticação ausente, inválido ou expirado.\"}");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
