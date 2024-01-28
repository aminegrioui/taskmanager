package com.aminejava.taskmanager.controller.subTask;

import com.aminejava.taskmanager.dto.subtask.SubTaskRequestDto;
import com.aminejava.taskmanager.dto.subtask.SubTaskUpdateDto;
import com.aminejava.taskmanager.model.SubTask;
import com.aminejava.taskmanager.services.subtasks.SubTaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taskmanager/v1/subTasks")
public class SubTaskController {

    private final SubTaskService subTaskService;

    public SubTaskController(SubTaskService subTaskService) {
        this.subTaskService = subTaskService;
    }

    @GetMapping("all/{taskId}")
    public ResponseEntity<?> getAllSubTasksOfTask(@PathVariable Long taskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(subTaskService.getSubTasksOfTask(taskId, requestHeader));
    }

    @GetMapping("/subTasksOfTask")
    public List<SubTask> getAllSubTasks(@RequestHeader HttpHeaders requestHeader) {
        return subTaskService.getAllSubTaskOfTheAuthenticated(requestHeader);
    }

    @PostMapping("/addSubTask")
    public ResponseEntity<?> saveSubTask(@RequestBody SubTaskRequestDto subTaskRequestDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subTaskService.saveSubTask(subTaskRequestDto, requestHeader));
    }

    @PutMapping("/updateSubTask/{subTaskId}")
    public ResponseEntity<?> updateSubTask(@PathVariable Long subTaskId, @RequestBody SubTaskUpdateDto subTaskUpdateDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(subTaskService.updateSubTask(subTaskId, subTaskUpdateDto, requestHeader));
    }

    @GetMapping("/{subTaskId}")
    public ResponseEntity<?> getSubTaskById(@PathVariable Long subTaskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(subTaskService.getSubTaskById(subTaskId, requestHeader));
    }

    @DeleteMapping("/{subTaskId}")
    public ResponseEntity<?> deleteSubTaskById(@PathVariable Long subTaskId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(subTaskService.deleteSubTaskById(subTaskId, requestHeader));
    }
}
