package com.quizspark.quizsparkserver.repositories;

import com.quizspark.quizsparkserver.models.DbFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FileRepository extends JpaRepository<DbFile, UUID> {
    List<DbFile> findByUserIdAndOriginFileIsNull(UUID userId);

    List<DbFile> findByOriginFileAndFileName(DbFile originFile, String fileName);

    @Modifying
    @Query("DELETE FROM DbFile f WHERE f.user IS NULL AND f.id IN :ids")
    void deleteFilesWithNullUserByIds(@Param("ids") List<UUID> ids);
}
