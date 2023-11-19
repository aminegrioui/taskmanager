package com.aminejava.taskmanager.dto.management;

import lombok.Data;

@Data
public class ToggleUser {
    private String username;
    private boolean toggle;
}
