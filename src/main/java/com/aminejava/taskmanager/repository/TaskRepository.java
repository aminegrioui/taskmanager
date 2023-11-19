package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.Project;
import com.aminejava.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByProject(Project project);
    Optional<Task> findByName(String name);
}