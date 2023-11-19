package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    Optional<SubTask> findByIdSubTask(Long idSubTask);

}