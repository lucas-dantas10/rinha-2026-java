package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TerminalRequest(
    @JsonProperty("is_online")
    boolean isOnline,
    @JsonProperty("card_present")
    boolean cardPresent,
    @JsonProperty("km_from_home")
    float kmFromHome
) {
}
