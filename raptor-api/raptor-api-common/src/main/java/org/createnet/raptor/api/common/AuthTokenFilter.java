package org.createnet.raptor.api.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.AuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class AuthTokenFilter extends GenericFilterBean {

    protected final static Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Value("${raptor.auth.header}")
    private String tokenHeader;

    @Value("${raptor.auth.headerPrefix}")
    private String tokenHeaderPrefix;
    
    @Value("${raptor.auth.url}")
    private String authUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String authToken = httpRequest.getHeader(this.tokenHeader);

        if (authToken != null && !authToken.isEmpty()) {
            try {
                
                logger.debug("Attempting token authentication..");
                
                Raptor r = new Raptor(authUrl, authToken);
                AuthClient.LoginState state = r.Auth().login();
                
                logger.debug("login ok, authenticated user `{}`", state.user.getUsername());
                
                UserDetails userDetails = new RaptorUserDetails(state.user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            }
            catch(Exception ex) {
                logger.debug("Login failed: {}", ex.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
