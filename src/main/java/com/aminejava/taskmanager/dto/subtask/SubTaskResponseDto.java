package com.aminejava.taskmanager.dto.subtask;

import com.aminejava.taskmanager.enums.State;
import lombok.Data;

@Data
public class SubTaskResponseDto {

    private String subTaskName;
    private String taskName;
    private State state;
    private Long taskId;
}
