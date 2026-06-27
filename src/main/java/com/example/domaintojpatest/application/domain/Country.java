package com.example.domaintojpatest.application.domain;

import lombok.Builder;

@Builder
public record Country(Long id, String name) {
}
