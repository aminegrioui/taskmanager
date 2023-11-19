package com.aminejava.taskmanager.dto.management.manager;

import lombok.Data;

import java.util.Set;

@Data
public class UserManagerProjectAffectationDto {

    private Long projectId;
    private Set<Long> userIdSet;
}
