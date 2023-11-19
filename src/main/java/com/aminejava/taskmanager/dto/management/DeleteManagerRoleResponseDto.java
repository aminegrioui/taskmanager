package com.aminejava.taskmanager.dto.management;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteManagerRoleResponseDto {

    private boolean isDeleted;
    private String description;
}
