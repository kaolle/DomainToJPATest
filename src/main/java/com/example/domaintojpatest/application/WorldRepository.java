package com.example.domaintojpatest.application;

import com.example.domaintojpatest.application.domain.World;

import java.util.List;
import java.util.Optional;

public interface WorldRepository {

    World save(World world);

    Optional<World> findById(Long id);

    List<World> findAll();

    void deleteById(Long id);

    boolean existsById(Long id);
}
