package org.createnet.raptor.auth.service.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;

@Component
public class JwtTokenService implements Serializable {

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

  public void refreshToken(Token token) {
    token.setCreated(new Date());
    token.setToken(generateToken(token));
  }
  
  public Token createToken(String name, User user, Long expires, String secret) {
    
    Token token = new Token();
    token.setUser(user);
    token.setExpires(expires);
    token.setSecret(secret);
    token.setName(name);
    token.setEnabled(true);
    
    final String tokenValue = generateToken(token, token.getExpiresInstant(), token.getSecret());
            
    return token;
  }
  
  public String generateToken(Token token) {
    return generateToken(token, token.getExpiresInstant(), token.getSecret());
  }
  
  public String generateToken(Token token, Instant expires, String secret) {
    
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
            .setExpiration(expiry == null ? null : Date.from(expiry))
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
  }

}
