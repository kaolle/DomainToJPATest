package com.example.domaintojpatest.persistance;

import com.example.domaintojpatest.application.WorldRepository;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.persistance.entity.WorldEntity;
import com.example.domaintojpatest.persistance.jpa.WorldJpaRepository;
import com.example.domaintojpatest.persistance.mapper.WorldMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
class WorldRepositoryImpl implements WorldRepository {

    private final WorldJpaRepository jpaRepository;
    private final WorldMapper mapper;

    WorldRepositoryImpl(WorldJpaRepository jpaRepository, WorldMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public World save(World world) {
        if (world.id() == null) {
            return mapper.toDomain(jpaRepository.save(mapper.toEntity(world)));
        }
        WorldEntity entity = jpaRepository.findById(world.id()).orElseThrow();
        mapper.updateEntity(entity, world);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<World> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<World> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }
}
