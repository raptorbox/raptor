package org.createnet.raptor.api.common.authentication;

import org.springframework.beans.factory.annotation.Value;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.createnet.raptor.sdk.Raptor;
import org.createnet.raptor.sdk.api.AuthClient;
import org.createnet.raptor.sdk.exception.AuthenticationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class TokenFilter extends OncePerRequestFilter {

    protected final static Logger logger = LoggerFactory.getLogger(TokenFilter.class);

    @Value("${raptor.auth.header}")
    private String tokenHeader;

    @Value("${raptor.auth.url}")
    private String authUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

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

            } catch (AuthenticationFailedException ex) {
                
                logger.debug("Invalid login token provided: {}", ex.getMessage());
                
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                return;
                
            } catch (Exception ex) {
                
                logger.warn("Login operation failure: {}", ex.getMessage());
                
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occured during authentication");
                return;
            }
        }

        chain.doFilter(request, response);

    }

}
