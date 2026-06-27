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
        return new World(entity.getId(), continents);
    }

    public WorldEntity toEntity(World world) {
        WorldEntity entity = new WorldEntity();
        List<ContinentEntity> continents = world.continents().stream()
                .map(c -> toContinentEntity(c, entity))
                .toList();
        entity.setContinents(new ArrayList<>(continents));
        return entity;
    }

    public void updateEntity(WorldEntity entity, World world) {
        // Remove continents no longer present
        entity.getContinents().removeIf(ce ->
                world.continents().stream().noneMatch(c -> ce.getId().equals(c.id())));

        world.continents().forEach(c -> {
            if (c.id() == null) {
                ContinentEntity ce = new ContinentEntity();
                ce.setName(c.name());
                ce.setWorld(entity);
                addNewCountries(ce, c.countries());
                entity.getContinents().add(ce);
            } else {
                entity.getContinents().stream()
                        .filter(ce -> ce.getId().equals(c.id()))
                        .findFirst()
                        .ifPresent(ce -> updateContinentInPlace(ce, c));
            }
        });
    }

    public void updateEntity(ContinentEntity entity, Continent continent) {
        entity.setName(continent.name());
        entity.getCountries().clear();
        continent.countries().forEach(c -> {
            CountryEntity country = new CountryEntity();
            country.setName(c.name());
            country.setContinent(entity);
            entity.getCountries().add(country);
        });
    }

    public Continent toContinentDomain(ContinentEntity entity) {
        List<Country> countries = entity.getCountries().stream()
                .map(c -> new Country(c.getId(), c.getName()))
                .toList();
        return new Continent(entity.getId(), entity.getName(), countries);
    }

    private void updateContinentInPlace(ContinentEntity entity, Continent continent) {
        entity.setName(continent.name());
        entity.getCountries().removeIf(ce ->
                continent.countries().stream().noneMatch(c -> ce.getId().equals(c.id())));
        addNewCountries(entity, continent.countries().stream().filter(c -> c.id() == null).toList());
    }

    private void addNewCountries(ContinentEntity entity, List<Country> countries) {
        countries.forEach(c -> {
            CountryEntity ce = new CountryEntity();
            ce.setName(c.name());
            ce.setContinent(entity);
            entity.getCountries().add(ce);
        });
    }

    private ContinentEntity toContinentEntity(Continent continent, WorldEntity worldEntity) {
        ContinentEntity entity = new ContinentEntity();
        entity.setName(continent.name());
        entity.setWorld(worldEntity);
        List<CountryEntity> countries = continent.countries().stream()
                .map(c -> toCountryEntity(c, entity))
                .toList();
        entity.setCountries(new ArrayList<>(countries));
        return entity;
    }

    private CountryEntity toCountryEntity(Country country, ContinentEntity continentEntity) {
        CountryEntity entity = new CountryEntity();
        entity.setName(country.name());
        entity.setContinent(continentEntity);
        return entity;
    }
}
