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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEntityParityTest {

    /**
     * Each entry: domain class, entity class,
     *   fieldMappings (domain field name -> entity field name for intentional renames),
     *   entityOnlyFields (entity fields with no domain equivalent, e.g. JPA back-references).
     */
    static Stream<Arguments> pairs() {
        return Stream.of(
                Arguments.of(World.class, WorldEntity.class, Map.of("nomre", "name"), Set.of()),
                Arguments.of(Continent.class, ContinentEntity.class, Map.of(), Set.of("world")),
                Arguments.of(Country.class, CountryEntity.class, Map.of(), Set.of("continent"))
        );
    }

    @ParameterizedTest(name = "{0} <-> {1}")
    @MethodSource("pairs")
    void everyDomainFieldExistsInEntity(Class<?> domain, Class<?> entity,
                                        Map<String, String> fieldMappings, Set<String> entityOnlyFields) {
        Set<String> entityFields = fieldNames(entity);
        Set<String> expected = fieldNames(domain).stream()
                .map(f -> fieldMappings.getOrDefault(f, f))
                .collect(Collectors.toSet());

        assertThat(entityFields)
                .as("Entity %s is missing fields from domain %s", entity.getSimpleName(), domain.getSimpleName())
                .containsAll(expected);
    }

    @ParameterizedTest(name = "{0} <-> {1}")
    @MethodSource("pairs")
    void everyEntityFieldExistsInDomain(Class<?> domain, Class<?> entity,
                                        Map<String, String> fieldMappings, Set<String> entityOnlyFields) {
        Map<String, String> reverseMapping = fieldMappings.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        Set<String> expected = fieldNames(entity).stream()
                .filter(f -> !entityOnlyFields.contains(f))
                .map(f -> reverseMapping.getOrDefault(f, f))
                .collect(Collectors.toSet());

        assertThat(fieldNames(domain))
                .as("Domain %s is missing fields from entity %s (excluding JPA-only: %s)",
                        domain.getSimpleName(), entity.getSimpleName(), entityOnlyFields)
                .containsAll(expected);
    }

    private Set<String> fieldNames(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }
}
