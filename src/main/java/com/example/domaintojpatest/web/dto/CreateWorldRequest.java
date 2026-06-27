package com.example.domaintojpatest.web.dto;

import java.util.List;

public record CreateWorldRequest(List<CreateContinentRequest> continents) {
}
