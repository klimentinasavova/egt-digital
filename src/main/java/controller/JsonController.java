package controller;

import model.Currency;
import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface JsonController {


    /***
     *
     * @param request - contains requestId, client, currency, timestamp
     * @return The current value of the specified currency by base EUR
     */
    public ResponseEntity<Double> getCurrencyInfo(CurrencyRequest request);

    /***
     *
     * @param request - contains requestId, client, currency, timestamp
     * @return Map of all currencies and corresponding values by the specified currency as base
     */
    public ResponseEntity<Map<String, Double>> getAllCurrenciesByBaseCurrency(CurrencyRequest request);

    /***
     *
     * @param request - contains requestId, client, currency, timestamp, period (in hours)
     * @return List of all records saved for the last 'period' of hours for the specified currency
     */
    public ResponseEntity<List<Currency>> getHistory(CurrencyHistoryRequest request);
}
