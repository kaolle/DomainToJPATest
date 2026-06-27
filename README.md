# Domain to JPA Mapping

A Spring Boot project demonstrating clean separation between a domain model and a JPA persistence layer.

---

## Core Principle

**Domain objects and JPA entities are kept strictly separate.** The domain model uses immutable Java records with no JPA annotations. All persistence concerns live in the `persistance` package and are never visible to the rest of the application.

Access to stored data flows exclusively through repository interfaces that speak the domain language:

```
REST layer  →  WorldRepository (interface, domain types)  →  persistance package  →  DB
```

---

## Why Separate Domain from JPA Entities?

Java records cannot be JPA entities — they are immutable and have no no-arg constructor. This forces the separation, but the benefits go beyond that constraint:

- Domain logic is free from ORM concerns (`@Entity`, `@Column`, cascade rules, etc.)
- Domain objects are easy to unit test with no Spring context
- Persistence strategy can change without touching domain code
- Mapper becomes the explicit contract between what the DB stores and what the application sees

The cost is that every new field must be added in three places: domain record, JPA entity, and mapper. The mapper completeness test (`WorldMapperTest`) catches this drift automatically.

---

## Package Structure

```
application/
  domain/
    World.java          ← Java record (immutable, no JPA)
    Continent.java      ← Java record
    Country.java        ← Java record
  WorldRepository.java  ← public interface, domain types only
  ContinentRepository.java

persistance/
  entity/
    WorldEntity.java    ← @Entity, managed by JPA
    ContinentEntity.java
    CountryEntity.java
  jpa/
    WorldJpaRepository.java      ← Spring Data, internal only
    ContinentJpaRepository.java
  mapper/
    WorldMapper.java    ← converts domain ↔ entity
  WorldRepositoryImpl.java       ← implements WorldRepository
  ContinentRepositoryImpl.java

web/
  WorldController.java
  dto/                  ← request DTOs (no domain IDs)
```

---

## Mapping Rules

### Creating new entities — `toEntity()`

Used only when saving a new domain object with no ID. Creates a fresh entity tree; IDs are never set since the database generates them.

```java
// WorldRepositoryImpl.save():
if (world.id() == null) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(world)));
}
```

### Updating existing entities — `updateEntity()`

Used when saving a domain object that already has an ID. The existing **managed** entity is loaded first, then mutated in place. This avoids Hibernate's `"A collection with cascade='all-delete-orphan' was no longer referenced"` error that occurs when you replace a tracked collection with a new list instance.

```java
// WorldRepositoryImpl.save():
WorldEntity entity = jpaRepository.findById(world.id()).orElseThrow();
mapper.updateEntity(entity, world);
return mapper.toDomain(jpaRepository.save(entity));
```

### Safe collection mutation

Collections must be **mutated in place** on managed entities, never replaced:

```java
// Correct — mutates the tracked collection
entity.getCountries().clear();
entity.getCountries().add(newCountry);

// Wrong — breaks orphan removal tracking (Hibernate-specific error)
entity.setCountries(new ArrayList<>(...));
```

### Owning side — always set the FK

In a bidirectional `@OneToMany` / `@ManyToOne` relationship, JPA writes the foreign key from the **owning side** (`@ManyToOne`). Always set it explicitly:

```java
countryEntity.setContinent(continentEntity);  // sets continent_id FK
continentEntity.setWorld(worldEntity);         // sets world_id FK
```

Forgetting this leaves the FK null in the database even after a successful save.

---

## Domain IDs

Domain records carry a nullable `Long id` field:
- `null` before the entity is persisted
- Populated after `save()` returns the mapped result

This lets callers reference stored objects by ID without coupling the domain to JPA identity management.

---

## Aggregate Roots and Repository Scope

`World` is the aggregate root. Operations that span the whole tree (create world, add/remove country) go through `WorldRepository`. Operations scoped to a single continent go through `ContinentRepository` directly — this avoids loading the entire world tree when only one continent needs updating.

`WorldRepository.save()` handles large object graphs but loads and diffs by ID to avoid re-inserting existing entities. `ContinentRepository.save()` is more efficient for continent-scoped edits with large country lists.

---

## Steps to Apply This Pattern

1. **Define domain records** in `application.domain` — pure data, no annotations.

2. **Add a nullable `Long id`** to each record to support CRUD identity after persistence.

3. **Create JPA entity classes** in `persistance/entity` — mutable classes with `@Entity`, `@Id`, `@GeneratedValue`, and relationship annotations.

4. **Declare repository interfaces** in `application` using domain types only (e.g. `save(World): World`). This is the public contract.

5. **Create Spring Data JPA repositories** in `persistance/jpa` — internal, not exposed outside the persistence layer.

6. **Write a mapper** (`WorldMapper`) with:
   - `toEntity(Domain)` — for creation only, never sets IDs
   - `updateEntity(Entity, Domain)` — for updates, mutates managed entity in place, always sets owning-side FK
   - `toDomain(Entity)` — converts back to immutable record

7. **Implement the repository interfaces** in `persistance`, branching on `id == null` to decide create vs update path.

8. **Write a mapper completeness test** (`WorldMapperTest`) using reflection to assert no null fields after mapping — this test fails automatically when a new field is added but not handled in the mapper or fixtures.
