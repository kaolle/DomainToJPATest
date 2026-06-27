package com.example.domaintojpatest.persistance.jpa;

import com.example.domaintojpatest.persistance.entity.ContinentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContinentJpaRepository extends JpaRepository<ContinentEntity, Long> {
}
