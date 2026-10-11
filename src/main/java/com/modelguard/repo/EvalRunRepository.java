package com.modelguard.repo;

import com.modelguard.entity.EvalRunEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvalRunRepository extends JpaRepository<EvalRunEntity, String> {
    Optional<EvalRunEntity> findFirstByOrderByCreatedAtDesc();
    List<EvalRunEntity> findAllByOrderByCreatedAtDesc(org.springframework.data.domain.Pageable pageable);
}
