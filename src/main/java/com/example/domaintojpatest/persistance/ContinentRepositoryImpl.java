package com.example.domaintojpatest.persistance;

import com.example.domaintojpatest.application.ContinentRepository;
import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.persistance.entity.ContinentEntity;
import com.example.domaintojpatest.persistance.jpa.ContinentJpaRepository;
import com.example.domaintojpatest.persistance.mapper.WorldMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
class ContinentRepositoryImpl implements ContinentRepository {

    private final ContinentJpaRepository jpaRepository;
    private final WorldMapper mapper;

    ContinentRepositoryImpl(ContinentJpaRepository jpaRepository, WorldMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Continent save(Continent continent) {
        ContinentEntity entity = jpaRepository.findById(continent.getId())
                .orElseThrow(() -> new EntityNotFoundException("Continent not found: " + continent.getId()));

        mapper.updateEntity(entity, continent);

        return mapper.toContinentDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Continent> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toContinentDomain);
    }
}
