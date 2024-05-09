package model.dto.requests;

public record CurrencyHistoryRequest(String requestId, String timestamp, String client, String currency, int period) {
}
