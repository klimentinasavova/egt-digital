package controller;

import model.Currency;
import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import repository.CurrenciesCurrent;
import repository.CurrenciesHistory;
import repository.RequestDB;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@Component
public class JsonControllerImpl implements JsonController {

    private RequestDB redis = new RequestDB();
    private CurrenciesCurrent currentCurrencies = CurrenciesCurrent.getCurrentCurrencies();
    private CurrenciesHistory currenciesDB = new CurrenciesHistory();

    @PostMapping("/json_api/current")
    @ResponseBody
    public double getCurrencyInfo(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();

        boolean successfullyAdded= redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        return value;
    }

    @PostMapping("/json_api/current/all")
    @ResponseBody
    public Map<String, Double> getAllCurrenciesByBaseCurrency(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        Double baseValue = currentCurrencies.getCurrency(currency);
        if (baseValue.isNaN()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        Map<String, Double> rates = new HashMap<>();
        currentCurrencies.getCurrentRates().forEach((key, value) -> {
            rates.put(key, value / baseValue);
        });

        return rates;
    }

    @PostMapping("/json_api/history")
    @ResponseBody
    public List<Currency> getHistory(@RequestBody CurrencyHistoryRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();
        int period = request.period();
        long beforeTimestamp = LocalDateTime.now().minusHours(period).toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }
        return history;
    }

    protected void cleanUp() {
        redis.close();
    }

}
