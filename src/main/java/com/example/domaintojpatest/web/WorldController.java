package com.example.domaintojpatest.web;

import com.example.domaintojpatest.application.ContinentRepository;
import com.example.domaintojpatest.application.WorldRepository;
import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.application.domain.Country;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.web.dto.CreateContinentRequest;
import com.example.domaintojpatest.web.dto.CreateCountryRequest;
import com.example.domaintojpatest.web.dto.CreateWorldRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/worlds")
class WorldController {

    private final WorldRepository worldRepository;
    private final ContinentRepository continentRepository;

    WorldController(WorldRepository worldRepository, ContinentRepository continentRepository) {
        this.worldRepository = worldRepository;
        this.continentRepository = continentRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public World createWorld(@RequestBody CreateWorldRequest request) {
        List<Continent> continents = request.continents().stream()
                .map(cr -> Continent.builder()
                        .name(cr.name())
                        .countries(cr.countries().stream()
                                .map(co -> Country.builder().name(co.name()).build())
                                .toList())
                        .build())
                .toList();
        return worldRepository.save(World.builder().continents(continents).build());
    }

    @GetMapping("/{id}")
    public World getWorld(@PathVariable Long id) {
        return worldRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public List<World> listWorlds() {
        return worldRepository.findAll();
    }

    @PutMapping("/{worldId}/continents/{continentId}")
    public Continent updateContinent(
            @PathVariable Long worldId,
            @PathVariable Long continentId,
            @RequestBody CreateContinentRequest request) {
        worldRepository.findById(worldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Continent updated = Continent.builder()
                .id(continentId)
                .name(request.name())
                .countries(request.countries().stream()
                        .map(cr -> Country.builder().name(cr.name()).build())
                        .toList())
                .build();

        return continentRepository.save(updated);
    }

    @PostMapping("/{worldId}/continents/{continentId}/countries")
    public World addCountry(
            @PathVariable Long worldId,
            @PathVariable Long continentId,
            @RequestBody CreateCountryRequest request) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Continent> updatedContinents = world.continents().stream()
                .map(c -> c.id().equals(continentId)
                        ? Continent.builder()
                                .id(c.id())
                                .name(c.name())
                                .countries(append(c.countries(), Country.builder().name(request.name()).build()))
                                .build()
                        : c)
                .toList();

        return worldRepository.save(World.builder().id(world.id()).continents(updatedContinents).build());
    }

    @DeleteMapping("/{worldId}/continents/{continentId}/countries/{countryId}")
    public World removeCountry(
            @PathVariable Long worldId,
            @PathVariable Long continentId,
            @PathVariable Long countryId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Continent> updatedContinents = world.continents().stream()
                .map(c -> c.id().equals(continentId)
                        ? Continent.builder()
                                .id(c.id())
                                .name(c.name())
                                .countries(c.countries().stream()
                                        .filter(co -> !co.id().equals(countryId))
                                        .toList())
                                .build()
                        : c)
                .toList();

        return worldRepository.save(World.builder().id(world.id()).continents(updatedContinents).build());
    }

    private static <T> List<T> append(List<T> list, T element) {
        List<T> result = new java.util.ArrayList<>(list);
        result.add(element);
        return result;
    }
}
