package com.aminejava.taskmanager.dto.management.manager;

import lombok.Data;

import java.util.Set;

@Data
public class GroupOfWorksResponseDto {
    private String projectManagerName;
    private Set<ShortUserResponseDto> users;
}
