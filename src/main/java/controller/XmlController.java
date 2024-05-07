package controller;

import model.CurrentCommand;
import model.HistoryCommand;
import model.responses.XmlCurrenciesByBaseResponse;
import model.responses.XmlCurrencyCurrentValueResponse;
import model.responses.XmlHistoryResponse;

import java.util.Map;
import java.util.NoSuchElementException;

public interface XmlController {

    /***
     *
     * @param request - contains requestId, consumer, currency
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return The current value of the specified currency by base EUR
     */
    public XmlCurrencyCurrentValueResponse getCurrencyInfo(CurrentCommand request);

    /***
     *
     * @param request - contains requestId, consumer, currency
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return Map of all currencies and corresponding values by the specified currency as base
     */
    public XmlCurrenciesByBaseResponse getAllCurrenciesByBaseCurrency(CurrentCommand request);

    /***
     *
     * @param request - contains requestId, client, currency, period (in hours)
     * @throws UnsupportedOperationException - if the requestId has already been used
     * @throws NoSuchElementException - if there is no information for the specified currency
     * @return List of all records saved for the last 'period' of hours for the specified currency
     */
    public XmlHistoryResponse getHistory(HistoryCommand request);

}
