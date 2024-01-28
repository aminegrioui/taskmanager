package com.aminejava.taskmanager.dto.project;

import com.aminejava.taskmanager.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortProjectResponseDto {
    private long projectId;
    private String projectName;
    private String description;
    private Priority priority;
}
