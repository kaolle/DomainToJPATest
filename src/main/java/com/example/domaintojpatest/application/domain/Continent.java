package com.example.domaintojpatest.application.domain;

import lombok.Builder;

import java.util.List;

@Builder
public record Continent(
        Long id,
        String name,
        List<Country> countries
) {
}
