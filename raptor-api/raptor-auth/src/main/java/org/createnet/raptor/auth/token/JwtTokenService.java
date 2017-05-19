package org.createnet.raptor.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.models.auth.Token;

@Component
public class JwtTokenService implements Serializable, TokenGenerator {

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_SOURCE = "source";
    private static final String CLAIM_KEY_CREATED = "created";
    private static final String CLAIM_KEY_UUID = "uuid";

    public Claims getClaims(Token token, String secret) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token.getToken())
                    .getBody();
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            claims = null;
        }
        return claims;
    }

    public Claims getClaims(Token token) {
        return getClaims(token, token.getSecret());
    }

    private void refreshToken(Token token) {
        token.setCreated(new Date());
        token.setToken(generateToken(token));
    }

    private String generateToken(Token token) {
        return generateToken(token, token.getExpiresInstant(), token.getSecret());
    }

    private String generateToken(Token token, Instant expires, String secret) {

        Map<String, Object> claims = new HashMap<>();

        claims.put(CLAIM_KEY_UUID, token.getUser().getUuid());
        claims.put(CLAIM_KEY_CREATED, token.getCreated().getTime() / 1000L);

        String tokenValue = generateToken(claims, secret, expires);
        token.setToken(tokenValue);

        return tokenValue;
    }

    private String generateToken(Map<String, Object> claims, String secret, Instant expiry) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(Date.from(expiry))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    @Override
    public String generate(Token t) {
        return generateToken(t);
    }

    @Override
    public boolean validate(Token t, String secret) {
        return (getClaims(t, secret) != null);
    }

}
