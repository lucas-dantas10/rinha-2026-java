package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MerchantRequest(
    String id,
    String mcc,
    @JsonProperty("avg_amount")
    float avgAmount
) {
}
