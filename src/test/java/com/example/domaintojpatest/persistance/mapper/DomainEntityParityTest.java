package com.example.domaintojpatest.persistance.mapper;

import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.application.domain.Country;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.persistance.entity.ContinentEntity;
import com.example.domaintojpatest.persistance.entity.CountryEntity;
import com.example.domaintojpatest.persistance.entity.WorldEntity;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntityParityTest {

    static Stream<Arguments> pairs() {
        return Stream.of(
                Arguments.of(World.class, WorldEntity.class),
                Arguments.of(Continent.class, ContinentEntity.class),
                Arguments.of(Country.class, CountryEntity.class)
        );
    }

    // Fields that exist only in the entity for JPA relationship navigation — no domain equivalent expected.
    static Stream<Arguments> pairsWithEntityOnlyFields() {
        return Stream.of(
                Arguments.of(WorldEntity.class, World.class, Set.of()),
                Arguments.of(ContinentEntity.class, Continent.class, Set.of("world")),
                Arguments.of(CountryEntity.class, Country.class, Set.of("continent"))
        );
    }

    @ParameterizedTest(name = "{0} <-> {1}")
    @MethodSource("pairs")
    void everyDomainFieldExistsInEntity(Class<?> domain, Class<?> entity) {
        Set<String> domainFields = fieldNames(domain);
        Set<String> entityFields = fieldNames(entity);

        assertThat(entityFields)
                .as("Entity %s is missing fields from domain %s", entity.getSimpleName(), domain.getSimpleName())
                .containsAll(domainFields);
    }

    @ParameterizedTest(name = "{0} <-> {1}")
    @MethodSource("pairsWithEntityOnlyFields")
    void everyEntityFieldExistsInDomain(Class<?> entity, Class<?> domain, Set<String> entityOnlyFields) {
        Set<String> entityFields = fieldNames(entity).stream()
                .filter(f -> !entityOnlyFields.contains(f))
                .collect(Collectors.toSet());
        Set<String> domainFields = fieldNames(domain);

        assertThat(domainFields)
                .as("Domain %s is missing fields from entity %s (excluding JPA-only: %s)",
                        domain.getSimpleName(), entity.getSimpleName(), entityOnlyFields)
                .containsAll(entityFields);
    }

    private Set<String> fieldNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
