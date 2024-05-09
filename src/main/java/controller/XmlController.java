package controller;

import model.dto.requests.XmlCurrentRequest;
import model.dto.requests.XmlRequest;
import model.dto.responses.XmlCurrenciesByBaseResponse;
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
