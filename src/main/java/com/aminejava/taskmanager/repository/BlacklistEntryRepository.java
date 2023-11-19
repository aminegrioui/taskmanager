package com.aminejava.taskmanager.repository;

import com.aminejava.taskmanager.system.entities.BlacklistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;

public interface BlacklistEntryRepository extends JpaRepository<BlacklistEntry, Long> {
    //    @Query("select  b from BlacklistEntry b where b.token LIKE %?1%")
    BlacklistEntry findByTokenLike(String bodyOfToken);

    BlacklistEntry findByUsername(String username);

    BlacklistEntry findByTokenAndExpiryTimeIsLessThanEqual(String token, ZonedDateTime nowTime);

    BlacklistEntry findByUsernameAndExpiryTimeIsLessThanEqual(String username, ZonedDateTime nowTime);
}