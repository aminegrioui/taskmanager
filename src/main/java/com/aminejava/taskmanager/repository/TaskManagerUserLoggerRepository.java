package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.system.entities.TaskManagerUserHistoric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface TaskManagerUserLoggerRepository extends JpaRepository<TaskManagerUserHistoric, Long> {
    @Query("select a from TaskManagerUserHistoric a where a.username=:username and a.operation=:operation and a.timestamp >= :startDate and a.timestamp <= :endDate order by a.timestamp desc")
    List<TaskManagerUserHistoric> findByUsernameAndTimestampBetweenDate(@Param("username") String username,
                                                                        @Param("startDate") ZonedDateTime startDate,
                                                                        @Param("endDate") ZonedDateTime endDate,
                                                                        @Param("operation") String operation);
}