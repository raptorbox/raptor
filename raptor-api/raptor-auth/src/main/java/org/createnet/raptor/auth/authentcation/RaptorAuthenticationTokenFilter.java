package org.createnet.raptor.auth.authentcation;

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
import org.createnet.raptor.api.common.authentication.RaptorUserDetails;
import org.createnet.raptor.api.common.configuration.TokenHelper;
import org.createnet.raptor.models.auth.Token;
import org.createnet.raptor.models.auth.User;
import org.createnet.raptor.auth.services.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RaptorAuthenticationTokenFilter extends GenericFilterBean {

    protected final static Logger logger = LoggerFactory.getLogger(RaptorAuthenticationTokenFilter.class);

    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private TokenHelper tokenHelper;

    @Value("${raptor.auth.header}")
    private String tokenHeader;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(this.tokenHeader);

        if (authToken != null && !authToken.isEmpty()) {
            
            authToken = tokenHelper.extractToken(authToken);
            Token token = tokenService.read(authToken);

            if (token != null) {
                boolean tokenIsValid = tokenService.isValid(token);
                if (tokenIsValid) {

                    User user = token.getUser() != null ? token.getUser() : null;
                    if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = new RaptorUserDetails(user);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

                } else {

                    logger.debug("Token is {} [name:`{}` id:{} type:{}]", (token.isExpired() ? "expired" : "not valid"), token.getName(), token.getId().toString(), token.getType().name());
                    if (token.isLoginToken()) {
                        tokenService.delete(token);
                    }

                }
            }

        }

        chain.doFilter(request, response);
    }
}
