package com.aminejava.taskmanager.controller.tasks;

import com.aminejava.taskmanager.dto.task.TaskAddDto;
import com.aminejava.taskmanager.dto.task.TaskUpdateDto;
import com.aminejava.taskmanager.services.tasks.TaskService;
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
    public ResponseEntity<?> getAllTasksOfAUser(@PathVariable Long projectId) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getAllTaskOfProject(projectId));
    }

    @PostMapping("/addTask")
    public ResponseEntity<?> saveTask(@RequestBody TaskAddDto taskDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.saveTask(taskDto));
    }

    @PutMapping("/updateTask")
    public ResponseEntity<?> updateTask(@RequestBody TaskUpdateDto taskDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.updateTask(taskDto));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.getTaskById(taskId));
    }


    @DeleteMapping("/deleteTask/{taskId}")
    public ResponseEntity<?> deleteTaskById(@PathVariable Long taskId) {
        return ResponseEntity.status(HttpStatus.OK).body(taskService.deleteTaskById(taskId));
    }
}
