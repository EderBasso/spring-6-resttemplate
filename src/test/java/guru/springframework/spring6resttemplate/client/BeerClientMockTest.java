package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerRestPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080/";

    BeerClient beerClient;
    MockRestServiceServer server;

    @Autowired
    RestTemplateBuilder restTemplateBuilder;

    @Autowired
    ObjectMapper objectMapper;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO beerDTO;
    String payload;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilder.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        beerDTO = getBeerDto();
        payload = objectMapper.writeValueAsString(beerDTO);

    }

    @Test
    void testListBeersQueryParameters() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_URL)
                .queryParam("beerName", "Ale")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestTo(uri))
                .andExpect(queryParam("beerName", "Ale"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> beerDTOPage = beerClient.listBeers("Ale", null, null, null, null);

        assertThat(beerDTOPage.getContent().size()).isGreaterThan(0);
    }

    @Test
    void testDeleteNotFound(){
        server.expect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(beerDTO.getId());
        });

        server.verify();

    }

    @Test
    void testDeleteBeer(){
        server.expect(method(HttpMethod.DELETE))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withNoContent());

        beerClient.deleteBeer(beerDTO.getId());

        server.verify();
    }

    @Test
    void testUpdateBeer(){
        server.expect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withNoContent());

        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        BeerDTO returnedBeerDto = beerClient.updateBeer(beerDTO);
        assertThat(returnedBeerDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testCreateBeer(){
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_URL)
                        .build(beerDTO.getId());

        server.expect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_URL))
                        .andRespond(withAccepted().location(uri));

        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        BeerDTO returnedBeerDto = beerClient.createBeer(beerDTO);
        assertThat(returnedBeerDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testGetBeerById(){
        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_URL, beerDTO.getId()))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        BeerDTO returnedBeerDto = beerClient.getBeerById(beerDTO.getId());
        assertThat(returnedBeerDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Basic dXNlcjE6cGFzc3dvcmQ="))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_URL))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);
    }

    BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mando Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("12345")
                .build();
    }

    BeerRestPageImpl getPage(){
        return new BeerRestPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }
}
