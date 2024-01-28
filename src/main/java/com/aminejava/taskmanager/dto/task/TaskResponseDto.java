package com.aminejava.taskmanager.dto.task;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.model.SubTask;
import lombok.Data;

import java.util.Set;

@Data
public class TaskResponseDto {
    private Long taskId;
    private Long projectId;
    private Long projectManagerId;
    private String name;
    private State state;
    private Priority priority;
    private String description;
    private Set<SubTask> subTaskSet;
}
