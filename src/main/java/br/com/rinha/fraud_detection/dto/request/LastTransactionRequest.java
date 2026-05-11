package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record LastTransactionRequest(
    Instant timestamp,
    @JsonProperty("km_from_current")
    float kmFromCurrent
) {
}
