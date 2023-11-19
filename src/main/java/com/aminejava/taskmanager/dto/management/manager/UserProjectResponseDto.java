package com.aminejava.taskmanager.dto.management.manager;

import lombok.Data;

import java.util.List;

@Data
public class UserProjectResponseDto {

    private String projectNameOfManager;
    private List<String> usernamesOfUsers;
    private String managerName;
    private String description;
}
