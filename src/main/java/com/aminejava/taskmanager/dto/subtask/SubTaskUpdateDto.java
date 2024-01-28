package com.aminejava.taskmanager.dto.subtask;

import com.aminejava.taskmanager.enums.State;
import lombok.Data;

@Data
public class SubTaskUpdateDto {

    private String subTaskName;
    private String description;

    private State state;
}
