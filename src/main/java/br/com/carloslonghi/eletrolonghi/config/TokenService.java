package br.com.carloslonghi.eletrolonghi.config;

import br.com.carloslonghi.eletrolonghi.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class TokenService {

    @Value("${spring.security.secret}")
    private String secret;

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        int oneDayInSeconds = 60 * 60 * 24;

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim("userId", user.getId())
                .withClaim("name", user.getName())
                .withExpiresAt(Instant.now().plusSeconds(oneDayInSeconds))
                .withIssuedAt(Instant.now())
                .withIssuer("API-EletroLonghi")
                .sign(algorithm);
    }
}
