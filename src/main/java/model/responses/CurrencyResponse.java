package model.responses;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public record CurrencyResponse (boolean success,
                                String timestamp,
                                String base,
                                String date,
                                ConcurrentHashMap<String, Double> rates){
}
