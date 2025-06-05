package br.com.carloslonghi.eletrolonghi.config;

import br.com.carloslonghi.eletrolonghi.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TokenService {

    @Value("${spring.security.secret}")
    private String secret;

    private final String userId = "userId";
    private final String userName = "userName";

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        int oneDayInSeconds = 60 * 60 * 24;

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim(userId, user.getId())
                .withClaim(userName, user.getName())
                .withExpiresAt(Instant.now().plusSeconds(oneDayInSeconds))
                .withIssuedAt(Instant.now())
                .withIssuer("API-EletroLonghi")
                .sign(algorithm);
    }

    public Optional<JWTUserData> verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            DecodedJWT jwt = JWT.require(algorithm)
                    .build()
                    .verify(token);

            JWTUserData jwtUserData = JWTUserData
                    .builder()
                    .id(jwt.getClaim(userId).asLong())
                    .name(jwt.getClaim(userName).asString())
                    .email(jwt.getSubject())
                    .build();

            return Optional.of(jwtUserData);
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}
