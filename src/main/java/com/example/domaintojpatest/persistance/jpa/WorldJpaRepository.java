package com.example.domaintojpatest.persistance.jpa;

import com.example.domaintojpatest.persistance.entity.WorldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldJpaRepository extends JpaRepository<WorldEntity, Long> {
}
