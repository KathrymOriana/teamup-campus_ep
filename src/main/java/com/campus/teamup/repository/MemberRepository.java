package com.campus.teamup.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.campus.teamup.model.Member;
import com.campus.teamup.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByUserId(Long userId);

    List<Member> findByProjectId(Long projectId);

    List<Member> findByProjectIdIn(Collection<Long> projectIds);

    Optional<Member> findByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRol(Long projectId, Role rol);

    void deleteByProjectId(Long projectId);
}
