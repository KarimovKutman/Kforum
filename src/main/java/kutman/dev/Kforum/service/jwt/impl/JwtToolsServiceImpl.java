package kutman.dev.Kforum.service.jwt.impl;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kutman.dev.Kforum.model.User;
import kutman.dev.Kforum.service.jwt.JwtToolsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;



@Service
public class JwtToolsServiceImpl implements JwtToolsService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public String generate(User user) {
        return Jwts
                .builder()
                .subject(user.getEmail())
                .claim("role", user.getRole().name()) // 🔑 Сохраняем роль в токен!
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
                .signWith(getSecretKey())
                .compact();
    }

    @Override
    public boolean validate(String token) {
        try {
            Jwts
                    .parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public String extractEmail(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public String extractRole(String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }


    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}
