package br.com.rinha.fraud_detection.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FraudScoreResponse(
    boolean approved,
    @JsonProperty("fraud_score")
    float fraudScore
) {
}
