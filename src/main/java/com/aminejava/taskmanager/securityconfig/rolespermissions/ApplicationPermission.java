package com.aminejava.taskmanager.securityconfig.rolespermissions;

import lombok.Getter;

@Getter
public enum ApplicationPermission {

    WRITE_USER("write:user"),

    READ_USER("read:user"),

    READ_MANAGER("read:manager"),

    WRITE_MANAGER("write:manager"),

    WRITE_ADMIN("write:admin"),

    READ_ADMIN("read:admin"),

    DISABLE_USER("disable:user"),

    ENABLE_USER("enable:user"),

    WRITE_PROJECT("write:project"),

    READ_PROJECT("read:project"),

    WRITE_SUBTASK("write:subtask"),

    READ_SUBTASK("read:subtask"),

    WRITE_TASK("write:task"),

    READ_TASK("read:task"),

    DISABLE_MANAGER_ROLE("disable:manager_role"),

    ENABLE_MANAGER_ROLE("enable:manager_role"),

    AFFECT_ROLE_PERMISSION("affect:role_permission"),

    WRITE_SUPER_ADMIN("write:super_admin"),

    READ_SUPER_ADMIN("read:super_admin"),

    AFFECT_USERS_TO_PROJECT("affect:users_to_project");

    private final String name;

    ApplicationPermission(String name) {
        this.name = name;
    }

}
