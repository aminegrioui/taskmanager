package com.aminejava.taskmanager.dto.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class DeleteTaskResponseDto {
    private boolean isDeleted;
    private String description;
}
