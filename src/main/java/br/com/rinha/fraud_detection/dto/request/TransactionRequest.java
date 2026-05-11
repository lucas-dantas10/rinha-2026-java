package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record TransactionRequest(
    float amount,
    int installments,
    @JsonProperty("requested_at")
    Instant requestedAt
) {
}
