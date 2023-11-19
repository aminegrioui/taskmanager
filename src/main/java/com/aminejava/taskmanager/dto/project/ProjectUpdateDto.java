package com.aminejava.taskmanager.dto.project;

import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.enums.Priority;
import lombok.Data;

import java.util.Set;

@Data
public class ProjectUpdateDto {

    private String nameProject;

    private String department;

    private String projectStart;

    private String projectEnd;

    private String description;

    private Priority priority;

    private Set<TaskAddDto> tasks;
}
