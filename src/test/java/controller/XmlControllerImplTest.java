package controller;

import model.*;
import model.dto.requests.XmlCurrentRequest;
import model.dto.requests.XmlHistoryRequest;
import model.dto.responses.XmlCurrenciesByBaseResponse;
import model.dto.responses.XmlCurrencyCurrentValueResponse;
import model.dto.responses.XmlHistoryResponse;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class XmlControllerImplTest {

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
    private XmlControllerImpl xmlController = spy(XmlControllerImpl.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static final String REQUEST_ID = "b89577fe-8c37-4962-8af3-7cb89a245160";
    private static final String CONSUMER = "13617162";
    private static final String TIMESTAMP = "1714992664";
    private static final int PERIOD = 1;
    private static final String CURRENCY = "USD";
    private static final double CURRENCY_VALUE = 1.076924;

    private static final ConcurrentHashMap<String, Double> RATES = new ConcurrentHashMap<>();

    @Test
    public void testGetCurrencyInfo_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity response = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetCurrencyInfo_withInvalidCurrency_shouldThrowNoSuchElementException() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);

        ResponseEntity response = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetCurrencyInfo_withValidCurrency_shouldSucceed() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(CURRENCY_VALUE);

        ResponseEntity<XmlCurrencyCurrentValueResponse> result = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(CURRENCY_VALUE, result.getBody().getValue());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity<XmlCurrenciesByBaseResponse> response = xmlController.getAllCurrenciesByBaseCurrency(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_withInvalidCurrency_shouldThrowNoSuchElementException() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);

        ResponseEntity<XmlCurrenciesByBaseResponse> response = xmlController.getAllCurrenciesByBaseCurrency(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllCurrenciesByBaseCurrency_withValidCurrency_shouldSucceed() {
        GetContent getContent = new GetContent();
        getContent.setCurrency(CURRENCY);
        getContent.setConsumer(CONSUMER);
        XmlCurrentRequest request = new XmlCurrentRequest();
        request.setGetCommand(getContent);
        request.setId(REQUEST_ID);

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

        XmlCurrenciesByBaseResponse result = xmlController.getAllCurrenciesByBaseCurrency(request).getBody();
        Assertions.assertEquals(result.getBase(), CURRENCY);
        Assertions.assertEquals(result.getRates().get("USD"), usdValue / usdValue);
        Assertions.assertEquals(result.getRates().get("EUR"), eurValue / usdValue);
        Assertions.assertEquals(result.getRates().get("RUB"), rubValue / usdValue);
    }

    @Test
    public void testGetHistory_whenRequestIdAlreadyUsed_shouldThrowUnsupportedOperationException() {
        HistoryContent historyContent = new HistoryContent();
        historyContent.setCurrency(CURRENCY);
        historyContent.setConsumer(CONSUMER);
        historyContent.setPeriod(PERIOD);
        XmlHistoryRequest request = new XmlHistoryRequest();
        request.setHistory(historyContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(false);

        ResponseEntity response = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetHistory_withNoInformationForCurrency_shouldThrowNoSuchElementException() {
        HistoryContent historyContent = new HistoryContent();
        historyContent.setCurrency(CURRENCY);
        historyContent.setConsumer(CONSUMER);
        historyContent.setPeriod(PERIOD);
        XmlHistoryRequest request = new XmlHistoryRequest();
        request.setHistory(historyContent);
        request.setId(REQUEST_ID);

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);
        when(currenciesHistoryMock.getCurrencyByPeriod(eq(CURRENCY), anyLong())).thenReturn(null);

        ResponseEntity response = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetHistory_withValidCurrency_shouldSucceed() {
        HistoryContent historyContent = new HistoryContent();
        historyContent.setCurrency(CURRENCY);
        historyContent.setConsumer(CONSUMER);
        historyContent.setPeriod(PERIOD);
        XmlHistoryRequest request = new XmlHistoryRequest();
        request.setHistory(historyContent);
        request.setId(REQUEST_ID);

        List<Currency> expected = new LinkedList<>();
        double value1 = CURRENCY_VALUE;
        double value2 = CURRENCY_VALUE + 0.005;
        double value3 = CURRENCY_VALUE + 0.01;

        expected.add(new Currency(TIMESTAMP, CURRENCY, value1));
        expected.add(new Currency(TIMESTAMP + 10, CURRENCY, value2));
        expected.add(new Currency(TIMESTAMP + 100, CURRENCY, value3));

        when(redisMock.addRequestId(REQUEST_ID)).thenReturn(true);
        when(currenciesCurrentMock.getCurrency(CURRENCY)).thenReturn(Double.NaN);
        when(currenciesHistoryMock.getCurrencyByPeriod(eq(CURRENCY), anyLong())).thenReturn(expected);

        ResponseEntity<XmlHistoryResponse> result = xmlController.getCurrencyInfo(request);
        Assertions.assertEquals(expected.size(), result.getBody().getHistory().size());
        Assertions.assertIterableEquals(expected, result.getBody().getHistory());
    }
}
