package com.aminejava.taskmanager.securityconfig.jwt;


import com.aminejava.taskmanager.dto.user.LoginResponseDto;
import com.aminejava.taskmanager.exception.user.UserNameNotFoundException;
import com.aminejava.taskmanager.model.Permission;
import com.aminejava.taskmanager.model.User;
import com.aminejava.taskmanager.model.admin.Admin;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.jwt.keys.KeyGeneratorTool;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.aminejava.taskmanager.securityconfig.rolespermissions.ApplicationRoles.USER;

@Service
@Log
public class JwtGenerator {

    private final KeyGeneratorTool keyGeneratorTool;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
//    @Getter
//    public ParseTokenResponse parseTokenResponse;

    public JwtGenerator(KeyGeneratorTool keyGeneratorTool, UserRepository userRepository, AdminRepository adminRepository) {
        this.keyGeneratorTool = keyGeneratorTool;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }


    @Bean
    private PrivateKey getPrivateKey() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPrivateKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPrivateKey();
        }
        return keyGeneratorTool.getPrivateKey();
    }

    @Bean
    private PublicKey getPublicKey() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPublicKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPublicKey();
        }
        return keyGeneratorTool.getPublicKey();
    }

    public LoginResponseDto generateAccessAndRefreshJwtToken(boolean isAdmin, Authentication authentication, long id, String username) throws IOException, NoSuchAlgorithmException {
        String authoritiesAsString = "";
        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            authoritiesAsString += grantedAuthority.getAuthority() + " ";
        }

        Date expireTimeOfAccessToken = Date.from(Instant.now().plus(45, ChronoUnit.MINUTES));
        log.info("The expire time (as created access token) of access Token is :" + expireTimeOfAccessToken);
        LoginResponseDto loginResponseDto = new LoginResponseDto();

        String accessToken = buildToken(isAdmin, id, username, authoritiesAsString, new Date(), expireTimeOfAccessToken);
        // refresh Token 7 Days
        Date expireTimeOfRefreshToken = Date.from(Instant.now().plus(24, ChronoUnit.HOURS));
        log.info("The expire time of refresh Token is :" + expireTimeOfRefreshToken);
        log.info("The issue time of refresh Token (first created) is :" + new Date());
        String refreshToken = buildToken(isAdmin, id, username, authoritiesAsString, new Date(), expireTimeOfRefreshToken, expireTimeOfAccessToken);
        loginResponseDto.setJwtRefreshToken(refreshToken);
        loginResponseDto.setJwtAccessToken(accessToken);
        loginResponseDto.setValid(true);
        return loginResponseDto;
    }

    public String buildToken(boolean isAdmin, long id, String username, String authorities, Date iat, Date exp) throws NoSuchAlgorithmException, IOException {
        String claimId = isAdmin ? "adminId" : "userId";
        return JWT.create()
                .withClaim(claimId, id)
                .withClaim("username", username)
                .withClaim("authorities", authorities)
                .withIssuedAt(iat)
                .withExpiresAt(exp)

                .sign(Algorithm.RSA512((RSAPublicKey) getPublicKey(), (RSAPrivateKey) getPrivateKey()));
    }

    public String buildToken(boolean isAdmin, long id, String username,
                             String authorities, Date iat,
                             Date expireTimeOfRefreshToken, Date expireTimeOfAccessToken) throws NoSuchAlgorithmException, IOException {
        String claimId = isAdmin ? "adminId" : "userId";
        return JWT.create()
                .withClaim(claimId, id)
                .withClaim("username", username)
                .withClaim("authorities", authorities)
                .withClaim("expAccessToken", expireTimeOfAccessToken)
                .withIssuedAt(iat)
                .withExpiresAt(expireTimeOfRefreshToken)

                .sign(Algorithm.RSA512((RSAPublicKey) getPublicKey(), (RSAPrivateKey) getPrivateKey()));
    }

    public LoginResponseDto generateNewAccessJwtToken(boolean isAdmin, String username,
                                                      Date expireTimeOfFreshToken,
                                                      Date expireTimeOfAccessToken, Date iat) throws IOException, NoSuchAlgorithmException {
        String authoritiesAsString = "";
        Collection<SimpleGrantedAuthority> grantedAuthorities;
        long id = 0;
        if (isAdmin) {
            Optional<Admin> optionalAdmin = adminRepository.findAdminByUsername(username);
            if (optionalAdmin.isEmpty()) {
                throw new UserNameNotFoundException("Admin is not in the system. No access Token can be generated ");
            }
            Admin admin = optionalAdmin.get();
            if (admin.isDeleted()) {
                throw new UserNameNotFoundException("Admin has been deleted. No access Token can be generated ");
            }
            id = admin.getAdminId();
            grantedAuthorities = admin.getPermissions().stream().map(permission -> new SimpleGrantedAuthority(permission.getPermission())).collect(Collectors.toSet());
            for (GrantedAuthority grantedAuthority : grantedAuthorities) {
                authoritiesAsString += grantedAuthority.getAuthority() + " ";
            }
        } else {
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                throw new UserNameNotFoundException("User is not in the system. No access Token can be generated ");
            }
            User user = optionalUser.get();
            if (user.isDeleted()) {
                throw new UserNameNotFoundException("User has been deleted. No access Token can be generated ");
            }
            id = user.getId();
            grantedAuthorities = new ArrayList<>();

            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + USER.name()));
            for (GrantedAuthority grantedAuthority : grantedAuthorities) {
                authoritiesAsString += grantedAuthority.getAuthority() + " ";
            }
        }

        LocalDateTime localDateTime = null;
        log.info("The expire time of access Token is :" + expireTimeOfAccessToken);
        if (expireTimeOfAccessToken != null) {
            localDateTime = expireTimeOfAccessToken.toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime().plusMinutes(45);
        }

        Date expire = localDateTime != null ? Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
                : Date.from(Instant.now().plus(45, ChronoUnit.MINUTES));
        if (expireTimeOfFreshToken.before(expire)) {
            expire = expireTimeOfFreshToken;
            log.info("The expire time of access Token is :" + expireTimeOfFreshToken + " this is last access token, which is automatically created using Refresh Token." +
                    "The User must log in again cause the expire time of refresh token will be ended in less than 15 minutes");
        }
        log.info("The new expire time of access Token is :" + expire);
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        String accessToken = buildToken(isAdmin, id, username, authoritiesAsString, new Date(), expire);
        // refresh Token 7 Days
        log.info("The issue time of refresh Token is :" + iat);
        log.info("The updated refresh Token has a new expire time of access token :" + expire);
        String updatedRefreshToken = buildToken(isAdmin, id, username, authoritiesAsString, iat, expireTimeOfFreshToken, expire);

        loginResponseDto.setJwtAccessToken(accessToken);
        loginResponseDto.setJwtRefreshToken(updatedRefreshToken);
        loginResponseDto.setValid(true);
        loginResponseDto.setGrantedAuthorities(grantedAuthorities);
        return loginResponseDto;
    }
}
