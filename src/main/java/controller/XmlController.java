package controller;

import model.requests.XmlCurrentRequest;
import model.requests.XmlRequest;
import model.responses.XmlCurrenciesByBaseResponse;
import org.springframework.http.ResponseEntity;

public interface XmlController {

    /***
     *
     * @param request - contains (requestId, consumer, currency) or (requestId, client, currency, period)
     * @return The current value of the specified currency by base EUR or
     * List of all records saved for the last 'period' of hours for the specified currency
     */
    public ResponseEntity getCurrencyInfo(XmlRequest request);

    /***
     *
     * @param request - contains requestId, consumer, currency
     * @return Map of all currencies and corresponding values by the specified currency as base
     */
    public ResponseEntity<XmlCurrenciesByBaseResponse> getAllCurrenciesByBaseCurrency(XmlCurrentRequest request);

}
