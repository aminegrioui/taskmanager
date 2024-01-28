package com.aminejava.taskmanager.securityconfig.jwt.filter;


import com.aminejava.taskmanager.dto.user.LoginResponseDto;
import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.securityconfig.jwt.JwtGenerator;
import com.aminejava.taskmanager.securityconfig.jwt.JwtTool;
import com.aminejava.taskmanager.securityconfig.jwt.ParseClaimsFromResponse;
import com.google.common.base.Strings;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Collection;

@Service
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTool jwtTool;
    private final JwtGenerator generator;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver resolver;

    public JwtTokenFilter(JwtTool jwtTool, JwtGenerator generator) {
        this.jwtTool = jwtTool;

        this.generator = generator;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String authorization = httpServletRequest.getHeader("Authorization_Refresh_Token");
        String token;
        boolean isRefreshToken = false;
        if (!Strings.isNullOrEmpty(authorization)) {
            token = authorization.replace("refreshToken ", "");
            isRefreshToken = true;
        } else {
            authorization = httpServletRequest.getHeader("Authorization");
            if (Strings.isNullOrEmpty(authorization) || !authorization.startsWith("Bearer ")) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
            token = authorization.replace("Bearer ", "");
        }


        try {
            Jws<Claims> claimsJws;
            claimsJws = jwtTool.verifyToken(token);
            ParseClaimsFromResponse parseClaimsFromResponse = jwtTool.parseUserNameAndAuthorities(claimsJws);

            String username = parseClaimsFromResponse.getUsername();
            Collection<? extends GrantedAuthority> grantedAuthorities = parseClaimsFromResponse.getPermissions();
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
            if (isRefreshToken) {

                LoginResponseDto loginResponseDto = generator.generateNewAccessJwtToken(parseClaimsFromResponse.isAdmin(),
                        parseClaimsFromResponse.getUsername(),
                        parseClaimsFromResponse.getExpireTimeOfRefreshToken(),
                        parseClaimsFromResponse.getExpireTimeOfAccessToken(), parseClaimsFromResponse.getIssueDateOfToken());
                httpServletResponse.addHeader("accessToken", loginResponseDto.getJwtAccessToken());
                httpServletResponse.addHeader("refreshToken", loginResponseDto.getJwtRefreshToken());
            }

            usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | BadCredentialsException |
                 ExpiredJwtException e) {
            resolver.resolveException(httpServletRequest, httpServletResponse, null, e);
        } catch (Exception ex) {
            resolver.resolveException(httpServletRequest, httpServletResponse, null, ex);
//            throw new GlobalException(ex.getMessage());
        }
    }
}
