package com.aminejava.taskmanager.securityconfig.jwt;

import lombok.Data;

@Data
public class ParseTokenResponse {

    private String username;
    private Long id;
    private boolean isAdmin;
    private boolean isSuperAdmin;
    private boolean isManager;
}
