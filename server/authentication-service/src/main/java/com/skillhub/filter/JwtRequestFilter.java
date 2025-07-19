package com.skillhub.filter;

import com.skillhub.service.CustomUserDetailsService;
import com.skillhub.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtUtil jwtUtil;

    /**
     * Constructor for JwtRequestFilter.
     *
     * @param customUserDetailsService the service to load user details
     * @param jwtUtil                  the utility class for JWT operations
     */
    @Autowired
    public JwtRequestFilter(CustomUserDetailsService customUserDetailsService, JwtUtil jwtUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * This method is called for every request to check if the JWT is valid and set the authentication in the security context.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param chain    the filter chain
     * @throws ServletException if an error occurs during filtering
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                LOG.warn("Error extracting JWT: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            try {
                            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);

                            if (jwtUtil.validateToken(jwt, userDetails)) {

                                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                usernamePasswordAuthenticationToken
                                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                            }
            }catch (UsernameNotFoundException e){
                LOG.warn("Auth failed: user {} of token not found", username, e);
            }

        }
        chain.doFilter(request, response);
    }
}