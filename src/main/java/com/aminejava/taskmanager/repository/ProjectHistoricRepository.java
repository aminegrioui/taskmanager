package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.ProjectHistoric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectHistoricRepository extends JpaRepository<ProjectHistoric, Long> {
}