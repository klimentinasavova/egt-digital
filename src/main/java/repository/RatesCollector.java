package repository;

import exceptions.CurrencyException;
import model.responses.CurrencyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Component
public class RatesCollector {

    @Value("${gateway.access.key}")
    protected String ACCESS_KEY ;
    private final String url = "http://data.fixer.io/api/latest?access_key=";

    protected RestTemplate restTemplate;

    @Autowired
    protected CurrenciesCurrent currentCurrencies;
    @Autowired
    protected CurrenciesHistory currenciesDB;


    public RatesCollector() {
        restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void collectCurrencyData() {

        if(ACCESS_KEY == null || ACCESS_KEY.isBlank()) {
            throw new IllegalArgumentException("No access key provided.");
        }
//        CurrencyResponse response = restTemplate.getForObject(url + ACCESS_KEY, CurrencyResponse.class);

//        Use for manual testing because of the limited request included in the free fixer.io account
        ConcurrentHashMap<String, Double> rates = new ConcurrentHashMap<>();
        rates.put("AED", 3.955554);
        rates.put("AFN", 77.789909);
        rates.put("ALL", 100.631706);
        rates.put("AMD", 417.119305);
        rates.put("ANG", 1.937773);
        rates.put("AOA", 900.308844);
        CurrencyResponse response = new CurrencyResponse(true, "1714992664", "EUR", "2024-05-06", rates);

        System.out.println(response);

        if (response == null || !response.success()) {
            throw new CurrencyException("Error retrieving currency data.");
        }

        // save currencies data to relational DB; will be used for get /history
        currenciesDB.saveCurrenciesData(response);

        // save currencies data to non-relational DB; will be used for get /current
        currentCurrencies.setCurrentRates(response.rates());

    }
}
