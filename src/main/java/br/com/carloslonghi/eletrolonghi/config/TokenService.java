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

    @Value("${spring.security.access-token-expiration-seconds:3600}")
    private long accessTokenExpirationSeconds;

    private static final String USER_ID = "userId";
    private static final String USER_NAME = "userName";
    private static final String USER_ROLE = "role";

    public String generateToken(User user) {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        return JWT.create()
                .withSubject(user.getEmail())
                .withClaim(USER_ID, user.getId())
                .withClaim(USER_NAME, user.getName())
                .withClaim(USER_ROLE, user.getRole().name())
                .withExpiresAt(Instant.now().plusSeconds(accessTokenExpirationSeconds))
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
                    .id(jwt.getClaim(USER_ID).asLong())
                    .name(jwt.getClaim(USER_NAME).asString())
                    .email(jwt.getSubject())
                    .role(jwt.getClaim(USER_ROLE).asString())
                    .build();

            return Optional.of(jwtUserData);
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}
