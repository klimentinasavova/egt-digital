package controller;


import model.Currency;
import model.CurrentCommand;
import model.HistoryCommand;
import model.responses.XmlCurrenciesByBaseResponse;
import model.responses.XmlCurrencyCurrentValueResponse;
import model.responses.XmlHistoryResponse;
import org.springframework.http.MediaType;
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
public class XmlControllerImpl implements XmlController {

    private RequestDB redis = new RequestDB();
    private CurrenciesCurrent currentCurrencies = CurrenciesCurrent.getCurrentCurrencies();
    private CurrenciesHistory currenciesDB = new CurrenciesHistory();

    @PostMapping(value = "/xml_api/command", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public XmlCurrencyCurrentValueResponse getCurrencyInfo(@RequestBody CurrentCommand request) {
        String requestId = request.getId();
        String client = request.getGetContent().getConsumer();
        String currency = request.getGetContent().getCurrency();

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        Double value = currentCurrencies.getCurrency(currency);
        if (value.isNaN()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }

        return new XmlCurrencyCurrentValueResponse(value);
    }

    @PostMapping(value = "/xml_api/command/all", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public XmlCurrenciesByBaseResponse getAllCurrenciesByBaseCurrency(@RequestBody CurrentCommand request) {
        String requestId = request.getId();
        String client = request.getGetContent().getConsumer();
        String currency = request.getGetContent().getCurrency();

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

        return new XmlCurrenciesByBaseResponse(rates, currency);
    }

    @PostMapping(value = "/xml_api/command", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public XmlHistoryResponse getHistory(@RequestBody HistoryCommand request) {
        String requestId = request.getId();
        String client = request.getHistory().getConsumer();
        String currency = request.getHistory().getCurrency();
        int period = request.getHistory().getPeriod();
        long beforeTimestamp = LocalDateTime.now().minusHours(period).toEpochSecond(ZoneOffset.UTC);

        boolean successfullyAdded = redis.addRequestId(requestId);
        if (!successfullyAdded) {
            throw new UnsupportedOperationException("Request id: " + requestId + " has already been used.");
        }

        List<Currency> history = currenciesDB.getCurrencyByPeriod(currency, beforeTimestamp);
        if (history == null || history.isEmpty()) {
            throw new NoSuchElementException("No information found for currency " + currency);
        }
        return new XmlHistoryResponse(history);
    }

}
