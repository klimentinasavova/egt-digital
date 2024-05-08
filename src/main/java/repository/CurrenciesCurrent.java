package repository;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class CurrenciesCurrent {

    //For storing current currencies values I have chosen an implementation with ConcurrentHashMap
    //Redis could also be used for these purposes

    private ConcurrentHashMap<String, Double> currentRates;

    public CurrenciesCurrent() {
        currentRates = new ConcurrentHashMap<>();
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
