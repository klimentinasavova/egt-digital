package controller;


import model.Currency;
import model.dto.requests.XmlCurrentRequest;
import model.dto.requests.XmlHistoryRequest;
import model.dto.requests.XmlRequest;
import model.dto.responses.XmlCurrenciesByBaseResponse;
import model.dto.responses.XmlCurrencyCurrentValueResponse;
import model.dto.responses.XmlHistoryResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final String CURRENCY_INFO_PATH = "/xml_api/command";
    private static final String CURRENCY_ALL_INFO_PATH = "/xml_api/command/all";

    private final Logger logger = LogManager.getLogger(XmlControllerImpl.class);

    @PostMapping(value = CURRENCY_INFO_PATH,
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

    @PostMapping(value = CURRENCY_ALL_INFO_PATH,
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<XmlCurrenciesByBaseResponse> getAllCurrenciesByBaseCurrency
            (@RequestBody XmlCurrentRequest request) {
        String requestId = request.getId();
        String currency = request.getGetContent().getCurrency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/xml_api/command/all: Request id : {} has already been used.", requestId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Double baseValue = currentCurrencies.getCurrency(currency);
        if (baseValue.isNaN()) {
            logger.debug("/xml_api/command/all: No information for currency: {}", currency);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Double> rates = new HashMap<>();
        currentCurrencies.getCurrentRates().forEach((key, value) -> {
            rates.put(key, value / baseValue);
        });

        logger.trace("/xml_api/command/all: Successfully retrieved information for currencies: {}", currency);
        sendStatisticsData(requestId, request.getGetContent().getConsumer());
        return ResponseEntity.ok(new XmlCurrenciesByBaseResponse(rates, currency));
    }

    private XmlCurrencyCurrentValueResponse getCurrencyInfo(XmlCurrentRequest request) {
        String requestId = request.getId();
        String currency = request.getGetContent().getCurrency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/xml_api/command: Request id : {} has already been used.", requestId);
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            logger.debug("/xml_api/command: No information for currency: {}", currency);
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        logger.trace("/xml_api/command: Successfully retrieved information for currency: {}", currency);
        sendStatisticsData(requestId, request.getGetContent().getConsumer());
        return new XmlCurrencyCurrentValueResponse(value);
    }

    private XmlHistoryResponse getHistory(XmlHistoryRequest request) {
        String requestId = request.getId();
        String currency = request.getHistory().getCurrency();
        int period = request.getHistory().getPeriod();
        long beforeTimestamp = LocalDateTime.now().minusHours(period).toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            logger.debug("/xml_api/command: Request id : {} has already been used.", requestId);
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            logger.debug("/xml_api/command: No information for currency: {}", currency);
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        logger.trace("/xml_api/command: Successfully retrieved history information for currency: {}", currency);
        sendStatisticsData(requestId, request.getHistory().getConsumer());
        return new XmlHistoryResponse(history);
    }

    private void sendStatisticsData(String requestId, String client) {
        long timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        rabbitMQSender.sendMessage(SERVICE_NAME + ", " + requestId + ", " + timestamp + ", " + client);
    }

}
