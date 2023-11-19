package com.aminejava.taskmanager.securityconfig.jwt;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class ParseClaimsFromResponse {
    private String username;
    private Collection<? extends GrantedAuthority> permissions;
}
