package model;

import java.util.Map;

public record Currency (String timestamp,
                       String currency,
                        Double value){
    //base is "EUR"
}
