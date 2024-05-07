package model.requests;

public record CurrencyRequest(String requestId, String timestamp, String client, String currency) {
}
