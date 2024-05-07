package repository;

import java.util.concurrent.ConcurrentHashMap;

public class CurrenciesCurrent {

    //TODO use redis cache

    private static CurrenciesCurrent currentCurrencies = new CurrenciesCurrent();
    private ConcurrentHashMap<String, Double> currentRates;


    private CurrenciesCurrent() {
    }

    public static CurrenciesCurrent getCurrentCurrencies() {
        return currentCurrencies;
    }

    public ConcurrentHashMap<String, Double> getCurrentRates() {
        return currentRates;
    }

    public void setCurrentRates(ConcurrentHashMap<String, Double> newRates) {
        currentRates = newRates;
    }

    public Double getCurrency(String currency) {
        return currentRates.getOrDefault(currency, Double.NaN);
    }
}
