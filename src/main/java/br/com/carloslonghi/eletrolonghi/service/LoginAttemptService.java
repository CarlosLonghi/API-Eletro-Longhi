package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.exception.TooManyLoginAttemptsException;
import br.com.carloslonghi.eletrolonghi.metrics.AuthMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory brute-force protection for the login endpoint.
 * Tracks failed attempts per key (email) and temporarily blocks after
 * a configurable threshold is exceeded.
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    @Value("${spring.security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${spring.security.login.block-duration-ms:900000}")
    private long blockDurationMs;

    private static final class Attempt {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile Instant blockedUntil;
    }

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    private final AuthMetrics authMetrics;

    public void checkBlocked(String key) {
        Attempt attempt = attempts.get(normalize(key));

        if (attempt != null && attempt.blockedUntil != null && Instant.now().isBefore(attempt.blockedUntil)) {
            authMetrics.blocked();
            throw new TooManyLoginAttemptsException(
                    "Muitas tentativas de login inválidas. Tente novamente mais tarde."
            );
        }
    }

    public void loginFailed(String key) {
        String normalizedKey = normalize(key);
        Attempt attempt = attempts.computeIfAbsent(normalizedKey, k -> new Attempt());
        int currentCount = attempt.count.incrementAndGet();
        authMetrics.failure();

        if (currentCount >= maxAttempts) {
            attempt.blockedUntil = Instant.now().plusMillis(blockDurationMs);
        }
    }

    public void loginSucceeded(String key) {
        attempts.remove(normalize(key));
        authMetrics.success();
    }

    private String normalize(String key) {
        return key == null ? "" : key.toLowerCase();
    }
}


