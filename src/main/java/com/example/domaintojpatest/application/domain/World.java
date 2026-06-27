package com.example.domaintojpatest.application.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record World(Long id, List<Continent> continents) {
}
