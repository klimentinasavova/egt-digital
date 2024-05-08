package controller;

import model.requests.CurrencyHistoryRequest;
import model.requests.CurrencyRequest;
import model.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import repository.CurrenciesCurrent;
import repository.CurrenciesHistory;
import repository.RabbitMQSender;
import repository.RequestDB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class JsonControllerImplTest {

    @Mock
    private RequestDB redisMock;

    @Mock
    private CurrenciesCurrent currenciesCurrentMock;

    @Mock
    private CurrenciesHistory currenciesHistoryMock;

    @Mock
    private RabbitMQSender rabbitMQSender;

    @InjectMocks
    @Spy
    private JsonControllerImpl jsonController = spy(JsonControllerImpl.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String TIMESTAMP = "1714992664";
    private static final String REQUEST_ID = "b89577fe-8c37-4962-8af3-7cb89a245160";
    private static final String CLIENT = "1234";
    private static final int PERIOD = 1;
    private static final String CURRENCY = "USD";
    private static final double CURRENCY_VALUE = 1.076924;
    private static final ConcurrentHashMap<String, Double> RATES = new ConcurrentHashMap<>();

    @Test
    public void testGetCurrencyInfo_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity<Double> result = jsonController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testGetCurrencyInfo_withInvalidCurrency_shouldThrowNoSuchElementException() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);

        ResponseEntity<Double> result = jsonController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testGetCurrencyInfo_withValidCurrency_shouldSucceed() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(CURRENCY_VALUE);

        ResponseEntity<Double> result = jsonController.getCurrencyInfo(request);
        Assertions.assertEquals(CURRENCY_VALUE, result.getBody());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity<Map<String, Double>> result = jsonController.getAllCurrenciesByBaseCurrency(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_withInvalidCurrency_shouldThrowNoSuchElementException() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);

        ResponseEntity<Map<String, Double>> result = jsonController.getAllCurrenciesByBaseCurrency(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_withValidCurrency_shouldSucceed() {
        CurrencyRequest request = new CurrencyRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY);
        ConcurrentHashMap<String, Double> currentRates = new ConcurrentHashMap<>();
        double eurValue = 1d;
        double rubValue = 98.651714;
        double usdValue = 1.076924;

        currentRates.put("EUR", eurValue);
        currentRates.put("RUB", rubValue);
        currentRates.put("USD", usdValue);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(CURRENCY_VALUE);
        when(currenciesCurrentMock.getCurrentRates()).thenReturn(currentRates);

        Map<String, Double> result = jsonController.getAllCurrenciesByBaseCurrency(request).getBody();
        Assertions.assertEquals(result.get("USD"), usdValue / usdValue);
        Assertions.assertEquals(result.get("EUR"), eurValue / usdValue);
        Assertions.assertEquals(result.get("RUB"), rubValue / usdValue);
    }

    @Test
    public void testGetHistory_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        CurrencyHistoryRequest request = new CurrencyHistoryRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY, PERIOD);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity<List<Currency>> result = jsonController.getHistory(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void testGetHistory_withNoInformationForCurrency_shouldThrowNoSuchElementException() {
        CurrencyHistoryRequest request = new CurrencyHistoryRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY, PERIOD);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);
        when(currenciesHistoryMock.getCurrencyByPeriod(eq(CURRENCY), anyLong())).thenReturn(null);

        ResponseEntity<List<Currency>> result = jsonController.getHistory(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testGetHistory_withValidCurrency_shouldSucceed() {
        CurrencyHistoryRequest request = new CurrencyHistoryRequest(REQUEST_ID, TIMESTAMP, CLIENT, CURRENCY, PERIOD);
        List<Currency> expected = new LinkedList<>();
        double value1 = CURRENCY_VALUE;
        double value2 = CURRENCY_VALUE + 0.005;
        double value3 = CURRENCY_VALUE + 0.01;

        expected.add(new Currency(TIMESTAMP, CURRENCY, value1));
        expected.add(new Currency(TIMESTAMP + 1, CURRENCY, value2));
        expected.add(new Currency(TIMESTAMP + 2, CURRENCY, value3));

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);
        when(currenciesHistoryMock.getCurrencyByPeriod(eq(CURRENCY), anyLong())).thenReturn(expected);

        List<Currency> result = jsonController.getHistory(request).getBody();
        Assertions.assertEquals(expected.size(), result.size());
        Assertions.assertIterableEquals(expected, result);
    }
}
