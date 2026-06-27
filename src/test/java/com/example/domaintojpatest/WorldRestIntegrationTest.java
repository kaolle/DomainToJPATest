package com.example.domaintojpatest;

import com.example.domaintojpatest.application.domain.Continent;
import com.example.domaintojpatest.application.domain.Country;
import com.example.domaintojpatest.application.domain.World;
import com.example.domaintojpatest.web.dto.CreateContinentRequest;
import com.example.domaintojpatest.web.dto.CreateCountryRequest;
import com.example.domaintojpatest.web.dto.CreateWorldRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorldRestIntegrationTest {

    @LocalServerPort
    int port;

    RestTemplate restTemplate = new RestTemplate();

    @Test
    void createAndGetWorld() {
        CreateWorldRequest request = new CreateWorldRequest(List.of(
                new CreateContinentRequest("Europe", List.of(
                        new CreateCountryRequest("Germany"),
                        new CreateCountryRequest("France")
                )),
                new CreateContinentRequest("Asia", List.of(
                        new CreateCountryRequest("Japan")
                ))
        ));

        ResponseEntity<World> createResponse = restTemplate.postForEntity(url("/worlds"), request, World.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        World created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.id()).isNotNull();
        assertThat(created.continents()).hasSize(2);

        ResponseEntity<World> getResponse = restTemplate.getForEntity(url("/worlds/{id}"), World.class, created.id());

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        World fetched = getResponse.getBody();
        assertThat(fetched).isNotNull();
        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.continents()).extracting(Continent::name)
                .containsExactlyInAnyOrder("Europe", "Asia");
    }

    @Test
    void updateContinent() {
        World world = createWorld();
        Continent europe = continentByName(world, "Europe");

        CreateContinentRequest updateRequest = new CreateContinentRequest("Europe", List.of(
                new CreateCountryRequest("Germany"),
                new CreateCountryRequest("France"),
                new CreateCountryRequest("Spain")
        ));

        ResponseEntity<Continent> updateResponse = restTemplate.exchange(
                url("/worlds/{worldId}/continents/{continentId}"),
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                Continent.class,
                world.id(), europe.id()
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Continent updated = updateResponse.getBody();
        assertThat(updated).isNotNull();
        assertThat(updated.countries()).hasSize(3);
        assertThat(updated.countries()).extracting(Country::name)
                .containsExactlyInAnyOrder("Germany", "France", "Spain");
    }

    @Test
    void addCountryToContinent() {
        World world = createWorld();
        Continent europe = continentByName(world, "Europe");

        ResponseEntity<World> response = restTemplate.postForEntity(
                url("/worlds/{worldId}/continents/{continentId}/countries"),
                new CreateCountryRequest("Spain"),
                World.class,
                world.id(), europe.id()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Continent updatedEurope = continentByName(response.getBody(), "Europe");
        assertThat(updatedEurope.countries()).hasSize(3);
        assertThat(updatedEurope.countries()).extracting(Country::name)
                .contains("Germany", "France", "Spain");
    }

    @Test
    void removeCountryFromContinent() {
        World world = createWorld();
        Continent europe = continentByName(world, "Europe");
        Country germany = europe.countries().stream()
                .filter(c -> c.name().equals("Germany"))
                .findFirst().orElseThrow();

        ResponseEntity<World> response = restTemplate.exchange(
                url("/worlds/{worldId}/continents/{continentId}/countries/{countryId}"),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                World.class,
                world.id(), europe.id(), germany.id()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Continent updatedEurope = continentByName(response.getBody(), "Europe");
        assertThat(updatedEurope.countries()).hasSize(1);
        assertThat(updatedEurope.countries()).extracting(Country::name).containsOnly("France");
    }

    private World createWorld() {
        CreateWorldRequest request = new CreateWorldRequest(List.of(
                new CreateContinentRequest("Europe", List.of(
                        new CreateCountryRequest("Germany"),
                        new CreateCountryRequest("France")
                )),
                new CreateContinentRequest("Asia", List.of(
                        new CreateCountryRequest("Japan")
                ))
        ));
        return restTemplate.postForEntity(url("/worlds"), request, World.class).getBody();
    }

    private Continent continentByName(World world, String name) {
        return world.continents().stream()
                .filter(c -> c.name().equals(name))
                .findFirst().orElseThrow();
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
