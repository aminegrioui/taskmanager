package com.aminejava.taskmanager.securityconfig.jwt.filter;

import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

@Service
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtGenerator jwtGenerator;

    public JwtTokenFilter(JwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String authorization = httpServletRequest.getHeader("Authorization");

        if (Strings.isNullOrEmpty(authorization) || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        String token = authorization.replace("Bearer ", "");

        Jws<Claims> claimsJws = jwtGenerator.verifyToken(token);
        HashMap<String, Collection<? extends GrantedAuthority>> map = jwtGenerator.parseUserNameAndAuthorities(claimsJws);

        String subject = (String) map.keySet().toArray()[0];
        Collection<? extends GrantedAuthority> grantedAuthorities = (Collection<? extends GrantedAuthority>) map.values().toArray()[0];
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(subject, null, grantedAuthorities);

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        filterChain.doFilter(httpServletRequest, httpServletResponse);

    }
}
