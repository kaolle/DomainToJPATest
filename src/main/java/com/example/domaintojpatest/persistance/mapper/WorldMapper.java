package com.example.domaintojpatest.persistance.mapper;

import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.application.domain.Country;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.persistance.entity.ContinentEntity;
import com.example.domaintojpatest.persistance.entity.CountryEntity;
import com.example.domaintojpatest.persistance.entity.WorldEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorldMapper {

    public World toDomain(WorldEntity entity) {
        List<Continent> continents = entity.getContinents().stream()
                .map(this::toContinentDomain)
                .toList();
        return World.builder().id(entity.getId()).continents(continents).build();
    }

    public WorldEntity toEntity(World world) {
        WorldEntity entity = new WorldEntity();
        List<ContinentEntity> continents = world.getContinents().stream()
                .map(c -> toContinentEntity(c, entity))
                .toList();
        entity.setContinents(new ArrayList<>(continents));
        return entity;
    }

    public void updateEntity(WorldEntity entity, World world) {
        entity.getContinents().removeIf(ce ->
                world.getContinents().stream().noneMatch(c -> ce.getId().equals(c.getId())));

        world.getContinents().forEach(c -> {
            if (c.getId() == null) {
                ContinentEntity ce = new ContinentEntity();
                ce.setName(c.getName());
                ce.setWorld(entity);
                updateEntity(ce, c);
                entity.getContinents().add(ce);
            } else {
                entity.getContinents().stream()
                        .filter(ce -> ce.getId().equals(c.getId()))
                        .findFirst()
                        .ifPresent(ce -> updateEntity(ce, c));
            }
        });
    }

    public void updateEntity(ContinentEntity entity, Continent continent) {
        entity.setName(continent.getName());
        entity.getCountries().clear();
        continent.getCountries().forEach(c -> {
            CountryEntity country = new CountryEntity();
            country.setName(c.getName());
            country.setContinent(entity);
            entity.getCountries().add(country);
        });
    }

    public Continent toContinentDomain(ContinentEntity entity) {
        List<Country> countries = entity.getCountries().stream()
                .map(c -> Country.builder().id(c.getId()).name(c.getName()).build())
                .toList();
        return Continent.builder().id(entity.getId()).name(entity.getName()).countries(countries).build();
    }

    private ContinentEntity toContinentEntity(Continent continent, WorldEntity worldEntity) {
        ContinentEntity entity = new ContinentEntity();
        entity.setName(continent.getName());
        entity.setWorld(worldEntity);
        List<CountryEntity> countries = continent.getCountries().stream()
                .map(c -> toCountryEntity(c, entity))
                .toList();
        entity.setCountries(new ArrayList<>(countries));
        return entity;
    }

    private CountryEntity toCountryEntity(Country country, ContinentEntity continentEntity) {
        CountryEntity entity = new CountryEntity();
        entity.setName(country.getName());
        entity.setContinent(continentEntity);
        return entity;
    }
}
