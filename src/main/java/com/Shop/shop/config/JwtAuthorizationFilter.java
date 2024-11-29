package com.Shop.shop.config;

import com.Shop.shop.service.TokenBlacklistService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    private final String secret;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
                                  UserDetailsService userDetailsService,
                                  TokenBlacklistService tokenBlacklistService,
                                  @Value("${jwt.secret}") String secret) {
        super(authenticationManager);
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.secret = secret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        if (authentication == null) {
            filterChain.doFilter(request, response);
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        if (token != null && token.startsWith(TOKEN_PREFIX)) {
            String jwtToken = token.replace(TOKEN_PREFIX, "");
            if (tokenBlacklistService.isTokenBlacklisted(jwtToken)) {
                return null;
            }
            String userName = JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();
            if (userName != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            }
        }
        return null;
    }
}
