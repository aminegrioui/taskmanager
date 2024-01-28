package com.aminejava.taskmanager.controller.tasks;

import com.aminejava.taskmanager.dto.task.TaskDto;
import com.aminejava.taskmanager.services.tasks.TaskService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/taskmanager/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping("/tasksOfProject/{projectId}")
    public ResponseEntity<?> getAllTasksOfAUser(@PathVariable Long projectId, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTaskOfProject(projectId, requestHeader));
    }

    @PostMapping("/addTask")
    public ResponseEntity<?> saveTask(@RequestBody TaskDto taskDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.saveTask(taskDto, requestHeader));
    }

    @PutMapping("/updateTask/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long taskId, @RequestBody TaskDto taskDto, @RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.updateTask(taskId, taskDto, requestHeader));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Long taskId,@RequestHeader HttpHeaders requestHeader) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getTaskById(taskId,requestHeader));
    }


    @DeleteMapping("/deleteTask/{taskId}")
    public ResponseEntity<?> deleteTaskById(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.deleteTaskById(taskId));
    }
}
