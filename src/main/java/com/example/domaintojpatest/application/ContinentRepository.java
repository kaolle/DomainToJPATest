package com.example.domaintojpatest.application;

import com.example.domaintojpatest.application.domain.Continent;

import java.util.Optional;

public interface ContinentRepository {

    Continent save(Continent continent);

    Optional<Continent> findById(Long id);
}
