package br.com.carloslonghi.eletrolonghi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenBearerTokenIsValid() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService);
        JWTUserData jwtUserData = JWTUserData.builder().id(1L).email("user@mail.com").name("User").role("ATENDENTE").build();

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenService.verifyToken("token")).thenReturn(Optional.of(jwtUserData));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldPassThroughWhenHeaderIsMissing() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenIsInvalid() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService);
        when(request.getHeader("Authorization")).thenReturn("Bearer expired");
        when(tokenService.verifyToken("expired")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldIgnoreHeaderWithoutBearerPrefix() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpzZW5oYQ==");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(tokenService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldAuthenticateWithoutAuthoritiesWhenRoleClaimIsMissing() throws Exception {
        SecurityFilter filter = new SecurityFilter(tokenService);
        JWTUserData jwtUserData = JWTUserData.builder().id(1L).email("user@mail.com").name("User").build();

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(tokenService.verifyToken("token")).thenReturn(Optional.of(jwtUserData));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEmpty();
        verify(filterChain).doFilter(request, response);
    }
}
