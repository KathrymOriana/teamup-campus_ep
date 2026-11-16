package com.campus.teamup.repository;

import java.util.Collection;
import java.util.List;

import com.campus.teamup.model.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectIdIn(Collection<Long> projectIds);

    void deleteByProjectId(Long projectId);

    @Modifying
    @Query("update Task t set t.responsableId = null where t.projectId = :projectId and t.responsableId = :userId")
    int unassign(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
