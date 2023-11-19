package com.aminejava.taskmanager.exception.user;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
