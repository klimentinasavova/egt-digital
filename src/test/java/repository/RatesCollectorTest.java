package repository;

import exceptions.CurrencyException;
import model.dto.responses.CurrencyResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class RatesCollectorTest {

    @Mock
    private RestTemplate restTemplateMock;

    @Mock
    private CurrenciesHistory currenciesHistoryMock;

    @Spy
    private CurrenciesCurrent currentCurrencies;

    @InjectMocks
    private RatesCollector ratesCollector;


    private static final boolean SUCCESS_TRUE = true;
    private static final String TIMESTAMP = "1714992664";
    private static final String BASE = "EUR";
    private static final String DATE = "2024-05-06";
    private static final ConcurrentHashMap<String, Double> RATES = new ConcurrentHashMap<>();

    @BeforeAll
    public static void initializeRates() {
        RATES.putIfAbsent("AED", 3.955554);
        RATES.putIfAbsent("AFN", 77.789909);
        RATES.putIfAbsent("ALL", 100.631706);
        RATES.putIfAbsent("AMD", 417.119305);
        RATES.putIfAbsent("ANG", 1.937773);
        RATES.putIfAbsent("USD", 1.076924);
    }

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ratesCollector.restTemplate = restTemplateMock;
        ratesCollector.ACCESS_KEY = "access_key";
    }

    @Test
    public void testCollectCurrencyData_whenValid_shouldSucceed() {
        when(restTemplateMock.getForObject(anyString(), eq(CurrencyResponse.class)))
                .thenReturn(new CurrencyResponse(SUCCESS_TRUE, TIMESTAMP, BASE, DATE, RATES));

        ratesCollector.collectCurrencyData();
        ConcurrentHashMap<String, Double> result = ratesCollector.currentCurrencies.getCurrentRates();

        verify(currenciesHistoryMock, times(1)).saveCurrenciesData(any(CurrencyResponse.class));
        assertIterableEquals(RATES.keySet(), result.keySet());
        assertEquals(RATES.get("USD"), ratesCollector.currentCurrencies.getCurrency("USD"));
    }

    @Test
    public void testCollectCurrencyData_withNoAccessKey_shouldThrowIllegalArgumentException() {
        ratesCollector.ACCESS_KEY = "";
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ratesCollector.collectCurrencyData());

        verify(currenciesHistoryMock, never()).saveCurrenciesData(any());
    }

    @Test
    public void testCollectCurrencyData_whenApiCallFails_shouldThrowCurrencyException() {
        when(restTemplateMock.getForObject(anyString(), eq(CurrencyResponse.class)))
                .thenReturn(null);

        assertThrows(CurrencyException.class, () -> ratesCollector.collectCurrencyData());
        verify(currenciesHistoryMock, never()).saveCurrenciesData(any());
    }

}
