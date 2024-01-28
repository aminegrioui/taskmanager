package com.aminejava.taskmanager.dto.task;


import com.aminejava.taskmanager.enums.State;
import lombok.Data;



@Data
public class TaskUpdateDto {
    private String name;
    private State state;
    private String description;
}
