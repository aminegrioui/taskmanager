package com.aminejava.taskmanager.dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProjectResponseDto {

    private boolean isDeleted;
    private String description;

}
