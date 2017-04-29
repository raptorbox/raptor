package org.createnet.raptor.api.common.authentication;

import org.springframework.beans.factory.annotation.Value;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.AuthClient;
import org.createnet.raptor.sdk.exception.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
public class TokenFilter extends GenericFilterBean {

    protected final static Logger logger = LoggerFactory.getLogger(TokenFilter.class);
    
    @Autowired
    public TokenHelper tokenHelper;
    
    @Value("${raptor.auth.header}")
    private String tokenHeader;

    @Value("${raptor.auth.url}")
    private String authUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(this.tokenHeader);
        
        if (authToken != null && !authToken.isEmpty()) {
            try {

                logger.debug("Attempting token authentication..");

                Raptor r = new Raptor(authUrl, tokenHelper.extractToken(authToken));
                AuthClient.LoginState state = r.Auth().login();

                logger.debug("login ok, authenticated user `{}`", state.user.getUsername());

                UserDetails userDetails = new RaptorUserDetails(state.user);

                LoginAuthenticationToken authentication = new LoginAuthenticationToken(userDetails, authToken, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (AuthenticationFailedException ex) {
                logger.debug("Invalid login token provided: {}", ex.getMessage());
            } catch (Exception ex) {
                logger.warn("Login operation failure: {}", ex.getMessage());
            }
        }

        chain.doFilter(request, response);

    }

}
