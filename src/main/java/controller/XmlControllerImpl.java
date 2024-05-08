package controller;


import model.Currency;
import model.requests.XmlCurrentRequest;
import model.requests.XmlHistoryRequest;
import model.requests.XmlRequest;
import model.responses.XmlCurrenciesByBaseResponse;
import model.responses.XmlCurrencyCurrentValueResponse;
import model.responses.XmlHistoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
public class XmlControllerImpl implements XmlController {

    @Autowired
    private RequestDB redis;
    @Autowired
    private CurrenciesCurrent currentCurrencies;
    @Autowired
    private CurrenciesHistory currenciesDB;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    private static final String SERVICE_NAME = "xml_service";

    @PostMapping(value = "/xml_api/command",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity getCurrencyInfo(@RequestBody XmlRequest request) {

        try {
            if ( request instanceof XmlCurrentRequest) {
                XmlCurrencyCurrentValueResponse response = getCurrencyInfo((XmlCurrentRequest) request);
                return ResponseEntity.ok(response);
            } else if (request instanceof XmlHistoryRequest) {
                XmlHistoryResponse response = getHistory((XmlHistoryRequest) request);
                return ResponseEntity.ok(response);
            }
        } catch (UnsupportedOperationException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/xml_api/command/all",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<XmlCurrenciesByBaseResponse> getAllCurrenciesByBaseCurrency
            (@RequestBody XmlCurrentRequest request) {
        String requestId = request.getId();
        String client = request.getGetContent().getConsumer();
        String currency = request.getGetContent().getCurrency();
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

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
        return ResponseEntity.ok(new XmlCurrenciesByBaseResponse(rates, currency));
    }

    private XmlCurrencyCurrentValueResponse getCurrencyInfo(XmlCurrentRequest request) {
        String requestId = request.getId();
        String client = request.getGetContent().getConsumer();
        String currency = request.getGetContent().getCurrency();
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }
        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
        return new XmlCurrencyCurrentValueResponse(value);
    }

    private XmlHistoryResponse getHistory(XmlHistoryRequest request) {
        String requestId = request.getId();
        String client = request.getHistory().getConsumer();
        String currency = request.getHistory().getCurrency();
        int period = request.getHistory().getPeriod();
        long beforeTimestamp = LocalDateTime.now().minusHours(period).toEpochSecond(ZoneOffset.UTC);
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
        return new XmlHistoryResponse(history);
    }
}
