package com.aminejava.taskmanager.dto.subtask;

import com.aminejava.taskmanager.enums.State;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubTaskResponseDto {

    private String subTaskName;
    private String taskName;
    private State state;
    private Long taskId ;
    private Long idSubTask;
    private String description;
}
