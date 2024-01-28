package com.aminejava.taskmanager.dto.task;

import com.aminejava.taskmanager.enums.Priority;
import com.aminejava.taskmanager.enums.State;
import lombok.Data;



@Data
public class TaskDto {

    private Long projectId;
    private Long projectManagerId;
    private String name;
    private State state;
    private Priority priority;
    private String description;
}
