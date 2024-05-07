package controller;

import model.Currency;
import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface JsonController {


    /***
     *
     * @param request - contains requestId, client, currency, timestamp
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return The current value of the specified currency by base EUR
     */
    public double getCurrencyInfo(CurrencyRequest request);

    /***
     *
     * @param request - contains requestId, client, currency, timestamp
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return Map of all currencies and corresponding values by the specified currency as base
     */
    public Map<String, Double> getAllCurrenciesByBaseCurrency(CurrencyRequest request);

    /***
     *
     * @param request - contains requestId, client, currency, timestamp, period (in hours)
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return List of all records saved for the last 'period' of hours for the specified currency
     */
    public List<Currency> getHistory(CurrencyHistoryRequest request);
}
