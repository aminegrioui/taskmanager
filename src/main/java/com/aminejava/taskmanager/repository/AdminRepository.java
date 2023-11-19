package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.model.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    @Query("select a from Admin as a where (a.username=:username or a.email=:email) and a.isDeleted=:deleted")
    Optional<Admin> findByUsernameOrEmailAndDeleted(@Param("username") String username, @Param("email") String email, @Param("deleted") boolean deleted);

    Optional<Admin> findAdminByUsername(String username);

    Optional<Admin> findAdminByAdminId(Long adminId);
}