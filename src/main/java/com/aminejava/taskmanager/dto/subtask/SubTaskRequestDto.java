package com.aminejava.taskmanager.dto.subtask;

import com.aminejava.taskmanager.enums.State;
import lombok.Data;

@Data
public class SubTaskRequestDto {

    private String subTaskName;
    private State state;
    private Long taskId;
    private String description;
}
