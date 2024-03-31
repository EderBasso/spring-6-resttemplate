package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerRestPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerClientImpl implements BeerClient {

    private final RestTemplateBuilder restTemplateBuilder;

    public static final String GET_BEER_URL = "api/v1/beer/";
    public static final String GET_BEER_BY_ID_URL = "api/v1/beer/{beerId}";

    @Override
    public void deleteBeer(UUID id) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.delete(GET_BEER_BY_ID_URL, id);
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.put(GET_BEER_BY_ID_URL, beerDTO, beerDTO.getId());
        return getBeerById(beerDTO.getId());
    }

    @Override
    public BeerDTO createBeer(BeerDTO beerDTO) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        //ResponseEntity<BeerDTO> responseEntity = restTemplate.postForEntity(GET_BEER_URL, beerDTO, BeerDTO.class);
        URI uri = restTemplate.postForLocation(GET_BEER_URL, beerDTO);

        return restTemplate.getForObject(uri.getPath(), BeerDTO.class);
    }

    @Override
    public BeerDTO getBeerById(UUID id){
        RestTemplate restTemplate = restTemplateBuilder.build();

        return restTemplate.getForObject(GET_BEER_BY_ID_URL, BeerDTO.class, id);

    }

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory,
                                   Integer pageNumber, Integer pageSize) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_BEER_URL);

        if (beerName != null){
            uriComponentsBuilder.queryParam("beerName", beerName);
        }
        if(beerStyle != null){
            uriComponentsBuilder.queryParam("beerStyle", beerStyle);
        }
        if(showInventory != null){
            uriComponentsBuilder.queryParam("showInventory", showInventory);
        }
        if(pageNumber != null){
            uriComponentsBuilder.queryParam("pageNumber", pageNumber);
        }
        if(pageSize != null){
            uriComponentsBuilder.queryParam("pageSize", pageSize);
        }

        ResponseEntity<BeerRestPageImpl> stringResponse = restTemplate.getForEntity(uriComponentsBuilder.toUriString(), BeerRestPageImpl.class);


        return stringResponse.getBody();
    }

    @Override
    public Page<BeerDTO> listBeers() {
        return this.listBeers(null, null, null, null, null);
    }
}
