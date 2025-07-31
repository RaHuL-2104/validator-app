package com.rahul.validator.filter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;

import com.rahul.validator.service.AppUserDetailsService;
import com.rahul.validator.util.JwtUtil;
import java.util.List;
import jakarta.servlet.http.Cookie;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter{

    private static final List<String> PUBLIC_URLS = List.of("/login", "/register", "/send-reset-otp", "/reset-password", "/logout");

    private final JwtUtil jwtUtil;
    private final AppUserDetailsService appUserDetailsService;

@Override
protected void doFilterInternal(
        @org.springframework.lang.NonNull HttpServletRequest request,
        @org.springframework.lang.NonNull HttpServletResponse response,
        @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {

    String path = request.getServletPath();
    if (PUBLIC_URLS.contains(path)) {
        filterChain.doFilter(request, response);
        return;
    }

    String jwt = null;
    String email = null;

    final String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer")) {
        jwt = authorizationHeader.substring(7);
    }

    if (jwt == null) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
    }

    if (jwt != null) {
        email = jwtUtil.extractEmail(jwt);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = appUserDetailsService.loadUserByUsername(email);
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
    }
    filterChain.doFilter(request, response);
}
}
