package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerRequest(
    @JsonProperty("avg_amount")
    float avgAmount,
    @JsonProperty("tx_count_24h")
    float txCount24h,
    @JsonProperty("known_merchant")
    String[] knownMerchant
) {
}
