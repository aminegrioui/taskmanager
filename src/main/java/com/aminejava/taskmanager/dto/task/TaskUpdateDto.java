package com.aminejava.taskmanager.dto.task;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import com.aminejava.taskmanager.model.SubTask;
import lombok.Data;

import java.util.Set;


@Data
public class TaskUpdateDto {
    private String name;
    private Priority priority;
    private State state;
    private String description;
    private String errorMessage;
    private Set<SubTask> subTaskSet;
}
