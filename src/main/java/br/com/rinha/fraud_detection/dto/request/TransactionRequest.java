package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionRequest(
    float amount,
    int installments,
    @JsonProperty("requested_at")
    String requestedAt
) {
}
