package br.com.rinha.fraud_detection.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

public record FraudScoreRequest(
    String id,
    TransactionRequest transaction,
    CustomerRequest customer,
    MerchantRequest merchant,
    TerminalRequest terminal,
    @JsonProperty("last_transaction")
    @Nullable
    LastTransactionRequest lastTransaction
) {
}
