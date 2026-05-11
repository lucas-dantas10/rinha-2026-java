package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LastTransactionRequest(
    String timestamp,
    @JsonProperty("km_from_current")
    float kmFromCurrent
) {
}
