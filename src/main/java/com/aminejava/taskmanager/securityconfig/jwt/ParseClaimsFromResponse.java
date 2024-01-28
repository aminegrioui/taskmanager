package com.aminejava.taskmanager.securityconfig.jwt;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;

@Data
public class ParseClaimsFromResponse {
    private Long id;
    private Date expireTimeOfRefreshToken;
    private Date expireTimeOfAccessToken;
    private Date issueDateOfToken;
    private boolean isAdmin;
    private String username;
    private Collection<? extends GrantedAuthority> permissions;
}
