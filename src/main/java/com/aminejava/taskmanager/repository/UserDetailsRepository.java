package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
}