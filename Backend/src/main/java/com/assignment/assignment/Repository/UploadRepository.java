package com.assignment.assignment.Repository;
import com.assignment.assignment.Class.Upload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;


public interface UploadRepository extends JpaRepository<Upload, Long> {

    @Query("SELECT COUNT(u) FROM Upload u WHERE u.imgurUserId = :userId AND u.uploadDate BETWEEN :start AND :end")
    int countDailyUploads(@Param("userId") String imgurUserId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);

}