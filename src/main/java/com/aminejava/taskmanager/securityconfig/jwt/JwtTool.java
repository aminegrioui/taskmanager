package com.aminejava.taskmanager.securityconfig.jwt;

import com.aminejava.taskmanager.exception.GlobalException;
import com.aminejava.taskmanager.securityconfig.jwt.keys.KeyGeneratorTool;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtTool {
    @Getter
    public ParseTokenResponse parseTokenResponse;

    private final KeyGeneratorTool keyGeneratorTool;

    public JwtTool(KeyGeneratorTool keyGeneratorTool) {
        this.keyGeneratorTool = keyGeneratorTool;
    }

    @Bean
    private PrivateKey getPrivateKey1() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPrivateKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPrivateKey();
        }
        return keyGeneratorTool.getPrivateKey();
    }

    @Bean
    private PublicKey getPublicKey1() throws NoSuchAlgorithmException, IOException {
        if (keyGeneratorTool.getPublicKey() == null) {
            keyGeneratorTool.createKeys();
            return keyGeneratorTool.getPublicKey();
        }
        return keyGeneratorTool.getPublicKey();
    }

    public Jws<Claims> verifyToken(String token) throws NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException {
        try {
            String[] partsArray = token.split("\\.");
            String parts = partsArray[0] + "." + partsArray[1];
            String sign = partsArray[2];
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify(getPublicKey1());
            signature.update(parts.getBytes());
            boolean isVerified = signature.verify(Base64.getUrlDecoder().decode(sign));
            if (!isVerified) {
                throw new BadCredentialsException("Untrusted Jwt ");
            }
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey1())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception ex) {
            throw new GlobalException(ex.getMessage());
        }

    }

    public ParseClaimsFromResponse parseUserNameAndAuthorities(Jws<Claims> claimsJws) {
        parseTokenResponse = new ParseTokenResponse();
        ParseClaimsFromResponse parseClaimsFromResponse = new ParseClaimsFromResponse();
        Claims body = claimsJws.getBody();
        String authorities = (String) body.get("authorities");
        String username = (String) body.get("username");
        Integer expireTimeOfRefreshToken = (Integer) body.get("exp");
        Integer expireTimeOfAccessToken = null;
        if (body.get("expAccessToken") != null) {
            expireTimeOfAccessToken = (Integer) body.get("expAccessToken");
        }

        Integer issueDateOfToken = (Integer) body.get("iat");
        String[] arrayStrings;
        Collection<? extends GrantedAuthority> simpleGrantedAuthorities = null;
        if (authorities != null) {
            arrayStrings = authorities.split(" ");
            simpleGrantedAuthorities = Arrays.stream(arrayStrings).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        }


        parseClaimsFromResponse.setUsername(username);
        parseClaimsFromResponse.setPermissions(simpleGrantedAuthorities);
        parseClaimsFromResponse.setExpireTimeOfRefreshToken(new Date(expireTimeOfRefreshToken * 1000l));
        parseClaimsFromResponse.setExpireTimeOfAccessToken(expireTimeOfAccessToken != null ? new Date(expireTimeOfAccessToken * 1000l) : null);
        parseClaimsFromResponse.setIssueDateOfToken(new Date(issueDateOfToken * 1000l));
        // Auth Info for this Request:
        Integer userIdValue = ((Integer) body.get("userId"));
        Integer adminIdValue = ((Integer) body.get("adminId"));


        parseTokenResponse.setUsername(username);
        if (userIdValue != null) {
            Long userId = userIdValue.longValue();
            parseTokenResponse.setId(userId);
            parseTokenResponse.setAdmin(false);
            parseClaimsFromResponse.setId(userId);
        } else {
            Long adminId = adminIdValue.longValue();
            parseClaimsFromResponse.setId(adminId);
            parseClaimsFromResponse.setAdmin(true);
            parseTokenResponse.setId(adminId);
            if (authorities != null) {
                parseTokenResponse.setAdmin(authorities.contains("ROLE_ADMIN"));
                parseTokenResponse.setManager(authorities.contains("ROLE_MANAGER"));
                parseTokenResponse.setSuperAdmin(authorities.contains("ROLE_SUPER_ADMIN"));
            }

        }
        return parseClaimsFromResponse;
    }

}
