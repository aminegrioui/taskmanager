package com.aminejava.taskmanager.securityconfig.jwt;

import com.aminejava.taskmanager.securityconfig.jwt.keys.KeyGenerator;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtGenerator {

    private final KeyGenerator keyGenerator;

    public JwtGenerator(KeyGenerator keyGenerator){
        this.keyGenerator = keyGenerator;
    }

    public String generateJwtToken(Authentication authentication) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        String authoritiesAsString = "";
        Collection<? extends GrantedAuthority> grantedAuthorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            authoritiesAsString += grantedAuthority.getAuthority() + " ";
        }
        return JWT.create()
                .withClaim("authorities", authoritiesAsString)
                .withIssuedAt(new Date())
                .withExpiresAt(java.sql.Date.valueOf(LocalDate.now().plusDays(1)))
                .sign(Algorithm.RSA512((RSAPublicKey) keyGenerator.loadPublicKey(), (RSAPrivateKey) keyGenerator.loadPrivateKey()));
    }

    public Jws<Claims> verifyToken(String token) {

        String[] array = token.split("\\.");
        String partsToken = array[1] + array[0];
        String sign = array[2];
        try {
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initVerify(keyGenerator.loadPublicKey());
            signature.update(partsToken.getBytes());
            signature.verify(Base64.getUrlDecoder().decode(sign));

            return Jwts.parserBuilder()
                    .setSigningKey(keyGenerator.loadPrivateKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | SignatureException e) {
            throw new IllegalStateException(String.format("Token %s cannot be truest", token));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, Collection<? extends GrantedAuthority>> parseUserNameAndAuthorities(Jws<Claims> claimsJws) {
        HashMap<String, Collection<? extends GrantedAuthority>> listMap = new HashMap<>();
        Claims body = claimsJws.getBody();
        String authorities = (String) body.get("authorities");
        String subject = body.getSubject();
        String[] arrayStrings = authorities.split(" ");
        Collection<? extends GrantedAuthority> simpleGrantedAuthorities = Arrays.stream(arrayStrings).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
        listMap.put(subject, simpleGrantedAuthorities);
        return listMap;

    }
}
