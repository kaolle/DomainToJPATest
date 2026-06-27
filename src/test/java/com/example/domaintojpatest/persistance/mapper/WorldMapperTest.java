package com.example.domaintojpatest.persistance.mapper;

import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.application.domain.Country;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.persistance.entity.ContinentEntity;
import com.example.domaintojpatest.persistance.entity.CountryEntity;
import com.example.domaintojpatest.persistance.entity.WorldEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WorldMapperTest {

    private final WorldMapper mapper = new WorldMapper();

    @Test
    void toDomainMapsAllFields() throws Exception {
        WorldEntity worldEntity = buildFullWorldEntity();

        World world = mapper.toDomain(worldEntity);

        assertNoNullRecordFields(world, Set.of());
    }

    @Test
    void toEntityMapsAllFieldsExceptId() throws Exception {
        World world = buildFullWorldDomain();

        WorldEntity entity = mapper.toEntity(world);

        assertNoNullEntityFields(entity, Set.of("id"), new HashSet<>());
    }

    @Test
    void updateWorldEntityMapsAllFields() throws Exception {
        WorldEntity existingEntity = buildFullWorldEntity(); // Europe (id=1) with Germany (id=1)

        World updatedWorld = World.builder()
                .id(1L)
                .continents(List.of(
                        Continent.builder()
                                .id(1L)                    // existing continent — updated in place
                                .name("Europe Renamed")
                                .countries(List.of(Country.builder().name("Spain").build())) // new country
                                .build(),
                        Continent.builder()
                                .name("Asia")              // new continent — no id
                                .countries(List.of(Country.builder().name("Japan").build()))
                                .build()
                ))
                .build();

        mapper.updateEntity(existingEntity, updatedWorld);

        assertThat(existingEntity.getContinents()).hasSize(2);

        ContinentEntity europe = continentById(existingEntity, 1L);
        assertThat(europe.getName()).isEqualTo("Europe Renamed");
        assertThat(europe.getCountries()).hasSize(1);
        assertThat(europe.getCountries().get(0).getName()).isEqualTo("Spain");
        assertThat(europe.getCountries().get(0).getContinent()).isSameAs(europe);

        ContinentEntity asia = existingEntity.getContinents().stream()
                .filter(c -> c.getId() == null).findFirst().orElseThrow();
        assertThat(asia.getName()).isEqualTo("Asia");
        assertThat(asia.getWorld()).isSameAs(existingEntity);
        assertThat(asia.getCountries().get(0).getContinent()).isSameAs(asia);

        assertNoNullEntityFields(existingEntity, Set.of("id"), new HashSet<>());
    }

    @Test
    void updateContinentEntityMapsAllFields() throws Exception {
        WorldEntity worldEntity = buildFullWorldEntity();
        ContinentEntity existingContinent = worldEntity.getContinents().get(0); // Europe with Germany

        Continent updatedContinent = Continent.builder()
                .id(1L)
                .name("Europe Renamed")
                .countries(List.of(Country.builder().name("France").build()))
                .build();

        mapper.updateEntity(existingContinent, updatedContinent);

        assertThat(existingContinent.getName()).isEqualTo("Europe Renamed");
        assertThat(existingContinent.getCountries()).hasSize(1);
        assertThat(existingContinent.getCountries().get(0).getName()).isEqualTo("France");
        assertThat(existingContinent.getCountries().get(0).getContinent()).isSameAs(existingContinent);

        assertNoNullEntityFields(existingContinent, Set.of("id"), new HashSet<>());
    }

    private ContinentEntity continentById(WorldEntity world, Long id) {
        return world.getContinents().stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst().orElseThrow();
    }

    private WorldEntity buildFullWorldEntity() {
        WorldEntity world = new WorldEntity();
        world.setId(1L);

        ContinentEntity continent = new ContinentEntity();
        continent.setId(1L);
        continent.setName("Europe");
        continent.setWorld(world);

        CountryEntity country = new CountryEntity();
        country.setId(1L);
        country.setName("Germany");
        country.setContinent(continent);

        continent.setCountries(new ArrayList<>(List.of(country)));
        world.setContinents(new ArrayList<>(List.of(continent)));
        return world;
    }

    private World buildFullWorldDomain() {
        return World.builder()
                .continents(List.of(
                        Continent.builder()
                                .name("Europe")
                                .countries(List.of(
                                        Country.builder().name("Germany").build()
                                ))
                                .build()
                ))
                .build();
    }

    // Recursively checks all record components are non-null (and strings non-blank, lists non-empty)
    private void assertNoNullRecordFields(Record record, Set<String> skip) throws Exception {
        for (RecordComponent comp : record.getClass().getRecordComponents()) {
            if (skip.contains(comp.getName())) continue;
            Object value = comp.getAccessor().invoke(record);
            assertThat(value)
                    .as("%s.%s must not be null", record.getClass().getSimpleName(), comp.getName())
                    .isNotNull();
            if (value instanceof String s) {
                assertThat(s)
                        .as("%s.%s must not be blank", record.getClass().getSimpleName(), comp.getName())
                        .isNotBlank();
            }
            if (value instanceof Collection<?> coll) {
                assertThat(coll)
                        .as("%s.%s must not be empty", record.getClass().getSimpleName(), comp.getName())
                        .isNotEmpty();
                for (Object item : coll) {
                    if (item instanceof Record r) assertNoNullRecordFields(r, skip);
                }
            }
        }
    }

    // Recursively checks entity fields non-null. Uses visited set per path to stop at back-references.
    private void assertNoNullEntityFields(Object entity, Set<String> skip, Set<Class<?>> visited) throws Exception {
        if (entity == null || visited.contains(entity.getClass())) return;
        visited.add(entity.getClass());

        for (Field field : entity.getClass().getDeclaredFields()) {
            if (skip.contains(field.getName())) continue;
            field.setAccessible(true);
            Object value = field.get(entity);
            assertThat(value)
                    .as("%s.%s must not be null", entity.getClass().getSimpleName(), field.getName())
                    .isNotNull();
            if (value instanceof String s) {
                assertThat(s)
                        .as("%s.%s must not be blank", entity.getClass().getSimpleName(), field.getName())
                        .isNotBlank();
            }
            if (value instanceof Collection<?> coll) {
                assertThat(coll)
                        .as("%s.%s must not be empty", entity.getClass().getSimpleName(), field.getName())
                        .isNotEmpty();
                for (Object item : coll) {
                    // Fresh visited copy per item so sibling entities of the same type are still checked
                    assertNoNullEntityFields(item, skip, new HashSet<>(visited));
                }
            }
        }
    }
}
