package com.example.domaintojpatest.web.dto;

import java.util.List;

public record CreateContinentRequest(String name, List<CreateCountryRequest> countries) {
}
