package org.createnet.raptor.auth.service.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.createnet.raptor.auth.service.RaptorUserDetailsService;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.service.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtAuthenticationTokenFilter extends GenericFilterBean {
  
  protected final static Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);
  
  public static String PREFIX = "Bearer ";

  @Autowired
  private TokenService tokenService;

  @Value("${jwt.header}")
  private String tokenHeader;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String authToken = httpRequest.getHeader(this.tokenHeader);

    if (authToken != null && !authToken.isEmpty()) {

      if (authToken.startsWith(PREFIX)) {
        authToken = authToken.substring(PREFIX.length());
      }

      Token token = tokenService.read(authToken);

      if (token != null) {
        boolean tokenIsValid = tokenService.isValid(token);
        if (tokenIsValid) {

          User user = token.getUser() != null ? token.getUser() : null;

          if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = new RaptorUserDetailsService.RaptorUserDetails(user);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
        else {
          logger.debug("JWT Token is {} [name:`{}` id:{} type:{}]", (token.isExpired() ? "expired" : "not valid"), token.getName(), token.getId().toString(), token.getType().name());
          if(token.isLoginToken()) {
            tokenService.delete(token);
          }
        }
      }
    }

    chain.doFilter(request, response);
  }
}
