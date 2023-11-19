package com.aminejava.taskmanager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteUserResponseDto {

    private boolean isDeleted;
    private String description;

}
