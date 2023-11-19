package com.aminejava.taskmanager.securityconfig;

import lombok.Data;

@Data
public class RequestAuthResponse {

    private Long id;
    private boolean isAuthorized;
    private boolean isAdmin;
}
