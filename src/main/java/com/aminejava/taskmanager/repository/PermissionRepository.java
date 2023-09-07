package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Permission findByPermission(String permission);
}
