package com.aminejava.taskmanager.dto.user;

import lombok.Data;

@Data
public class RefreshTokenDto {
    private String jwtAccessToken;
    private String jwtRefreshToken;
}
