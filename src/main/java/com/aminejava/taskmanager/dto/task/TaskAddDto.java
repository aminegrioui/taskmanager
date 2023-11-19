package com.aminejava.taskmanager.dto.task;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import lombok.Data;



@Data
public class TaskAddDto {

    private Long projectId;
    private Long projectManagerId;
    private String name;
    private Priority priority;
    private State state;
    private String description;
    private String errorMessage;
}
