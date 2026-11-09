package io.garage.auth;

import io.garage.domain.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;

@ApplicationScoped
public class TokenService {

    public String generateToken(User user) {
        return Jwt
                .subject(user.id.toString())
                .claim("username", user.username)
                .claim("email", user.email)
                .upn(user.email)
                .groups(user.role.toString())
                .expiresIn(Duration.ofHours(8))
                .sign();
    }
}
