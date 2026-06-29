package com.example.domaintojpatest.application.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class World {
    private Long id;
    private String nomre;
    private List<Continent> continents;
}
