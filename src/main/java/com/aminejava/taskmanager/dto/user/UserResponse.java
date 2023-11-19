package com.aminejava.taskmanager.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
public class UserResponse {

    private String username;
    private String description;
    @JsonIgnore
    private HttpHeaders httpHeaders;
    private String jwt;
}
