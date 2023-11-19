package com.aminejava.taskmanager.dto.subtask;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteSubTaskDto {
    private boolean isDeleted;
    private String description;
}
