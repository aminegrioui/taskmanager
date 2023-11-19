package com.aminejava.taskmanager.securityconfig.jwt;


import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.repository.AdminRepository;
import com.aminejava.taskmanager.repository.SubTaskRepository;
import com.aminejava.taskmanager.repository.UserRepository;
import com.aminejava.taskmanager.securityconfig.RequestAuthResponse;
import com.aminejava.taskmanager.securityconfig.jwt.keys.KeyGeneratorTool;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.rmi.ServerError;
import java.security.*;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtGenerator {

    private final KeyGeneratorTool keyGeneratorTool;
    @Getter
    public ParseTokenResponse parseTokenResponse;

    public JwtGenerator(KeyGeneratorTool keyGeneratorTool) {
        this.keyGeneratorTool = keyGeneratorTool;
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

    public String generateJwtToken(boolean isAdmin, Authentication authentication, long id, String username) throws IOException, NoSuchAlgorithmException {
        String authoritiesAsString = "";
        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            authoritiesAsString += grantedAuthority.getAuthority() + " ";
        }
        String claimId = isAdmin ? "adminId" : "userId";
        long ex = new Date().getTime() + 2 * 60 * 1000;
        Date expire = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
//        Date expire = new Date(ex);
        return JWT.create()
                .withClaim(claimId, id)
                .withClaim("username", username)
                .withClaim("authorities", authoritiesAsString)
                .withIssuedAt(new Date())
                .withExpiresAt(expire)
                .sign(Algorithm.RSA512((RSAPublicKey) getPublicKey(), (RSAPrivateKey) getPrivateKey()));
    }


    public Jws<Claims> verifyToken(String token) throws NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {

        String[] partsArray = token.split("\\.");
        String parts = partsArray[0] + "." + partsArray[1];
        String sign = partsArray[2];
        Signature signature = Signature.getInstance("SHA512withRSA");
        signature.initVerify(getPublicKey());
        signature.update(parts.getBytes());
        boolean isVerified = signature.verify(Base64.getUrlDecoder().decode(sign));
        if (!isVerified) {
            throw new BadCredentialsException("Untrusted Jwt ");
        }
        return Jwts.parserBuilder()
                .setSigningKey(getPrivateKey())
                .build()
                .parseClaimsJws(token);
    }

    public ParseClaimsFromResponse parseUserNameAndAuthorities(Jws<Claims> claimsJws) {
        parseTokenResponse = new ParseTokenResponse();
        ParseClaimsFromResponse parseClaimsFromResponse = new ParseClaimsFromResponse();
        Claims body = claimsJws.getBody();
        String authorities = (String) body.get("authorities");
        String username = (String) body.get("username");
        String[] arrayStrings = authorities.split(" ");
        Collection<? extends GrantedAuthority> simpleGrantedAuthorities = Arrays.stream(arrayStrings).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        parseClaimsFromResponse.setUsername(username);
        parseClaimsFromResponse.setPermissions(simpleGrantedAuthorities);

        // Auth Info for this Request:
        Integer userIdValue = ((Integer) body.get("userId"));
        Integer adminIdValue = ((Integer) body.get("adminId"));


        parseTokenResponse.setUsername(username);
        if (userIdValue != null) {
            Long userId = userIdValue.longValue();
            parseTokenResponse.setId(userId);
            parseTokenResponse.setAdmin(false);
        } else {
            Long adminId = adminIdValue.longValue();
            parseTokenResponse.setId(adminId);
            parseTokenResponse.setAdmin(authorities.contains("ROLE_ADMIN"));
            parseTokenResponse.setManager(authorities.contains("ROLE_MANAGER"));
            parseTokenResponse.setSuperAdmin(authorities.contains("ROLE_SUPER_ADMIN"));
        }
        return parseClaimsFromResponse;
    }

}
