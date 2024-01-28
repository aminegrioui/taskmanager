package com.aminejava.taskmanager.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

// Created cause the frontend side don't accept jwt as string
@Data
public class LoginResponseDto {
    private String jwtAccessToken;
    private String jwtRefreshToken;
    private boolean isValid;
    @JsonIgnore
    Collection<? extends GrantedAuthority> grantedAuthorities;
}
