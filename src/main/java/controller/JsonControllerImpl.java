package controller;

import model.Currency;
import model.dto.requests.CurrencyHistoryRequest;
import model.dto.requests.CurrencyRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final String CURRENCY_INFO_PATH = "/json_api/current";
    private static final String CURRENCY_ALL_INFO_PATH = "/json_api/current/all";
    private static final String CURRENCY_HISTORY_PATH = "/json_api/history";

    private final Logger logger = LogManager.getLogger(JsonControllerImpl.class);

    @PostMapping(CURRENCY_INFO_PATH)
    @ResponseBody
    public ResponseEntity<Double> getCurrencyInfo(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String currency = request.currency();

        boolean successfullyAdded= redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/json_api/current: Request id : {} has already been used.", requestId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            logger.debug("/json_api/current: No information for currency: {}", currency);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.trace("/json_api/current: Successfully retrieved information for currency: {}", currency);
        sendStatisticsData(requestId, request.timestamp(), request.client(), currency);
        return ResponseEntity.ok(value);
    }

    @PostMapping(CURRENCY_ALL_INFO_PATH)
    @ResponseBody
    public ResponseEntity<Map<String, Double>> getAllCurrenciesByBaseCurrency(@RequestBody CurrencyRequest request) {
        String requestId = request.requestId();
        String currency = request.currency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/json_api/current/all: Request id : {} has already been used.", requestId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double baseValue = currentCurrencies.getCurrency(currency);
        if (baseValue.isNaN()) {
            logger.debug("/json_api/current/all: No information for currency: {}", currency);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Double> rates = new HashMap<>();
        currentCurrencies.getCurrentRates().forEach((key, value) -> {
            rates.put(key, value / baseValue);
        });

        logger.trace("/json_api/current/all: Successfully retrieved information for currencies: {}", currency);
        sendStatisticsData(requestId, request.timestamp(), request.client(), currency);
        return ResponseEntity.ok(rates);
    }

    @PostMapping(CURRENCY_HISTORY_PATH)
    @ResponseBody
    public ResponseEntity<List<Currency>> getHistory(@RequestBody CurrencyHistoryRequest request) {
        String requestId = request.requestId();
        String currency = request.currency();
        long beforeTimestamp = LocalDateTime.now().minusHours(request.period()).toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/json_api/history: Request id : {} has already been used.", requestId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            logger.debug("/json_api/history: No information for currency: {}", currency);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        logger.trace("/json_api/history: Successfully retrieved history information for currency: {}", currency);
        sendStatisticsData(requestId, request.timestamp(), request.client(), currency);
        return ResponseEntity.ok(history);
    }

    private void sendStatisticsData(String requestId, String timestamp, String client, String currency) {
        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
    }

    protected void cleanUp() {
        redis.close();
    }

}
