package controller;

import model.Currency;
import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import repository.CurrenciesCurrent;
import repository.CurrenciesHistory;
import repository.RabbitMQSender;
import repository.RequestDB;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
public class JsonControllerImpl implements JsonController {

    @Autowired
    private RequestDB redis;
    @Autowired
    private CurrenciesCurrent currentCurrencies;
    @Autowired
    private CurrenciesHistory currenciesDB;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    private static final String SERVICE_NAME = "json_service";

    @PostMapping("/json_api/current")
    @ResponseBody
    public ResponseEntity<Double> getCurrencyInfo(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();

        boolean successfullyAdded= redis.addRequestId(requestId);
        if (!successfullyAdded) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
        return ResponseEntity.ok(value);
    }

    @PostMapping("/json_api/current/all")
    @ResponseBody
    public ResponseEntity<Map<String, Double>> getAllCurrenciesByBaseCurrency(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double baseValue = currentCurrencies.getCurrency(currency);
        if (baseValue.isNaN()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Double> rates = new HashMap<>();
        currentCurrencies.getCurrentRates().forEach((key, value) -> {
            rates.put(key, value / baseValue);
        });

        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
        return ResponseEntity.ok(rates);
    }

    @PostMapping("/json_api/history")
    @ResponseBody
    public ResponseEntity<List<Currency>> getHistory(@RequestBody CurrencyHistoryRequest request) {
        String requestId = request.requestId();
        String timestamp = request.timestamp();
        String client = request.client();
        String currency = request.currency();
        int period = request.period();
        long beforeTimestamp = LocalDateTime.now().minusHours(period).toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
        return ResponseEntity.ok(history);
    }

    protected void cleanUp() {
        redis.close();
    }

}
