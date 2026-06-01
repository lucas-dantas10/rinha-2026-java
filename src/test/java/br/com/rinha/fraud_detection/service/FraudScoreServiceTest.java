package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.TransactionVector;
import br.com.rinha.fraud_detection.dto.request.CustomerRequest;
import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.request.MerchantRequest;
import br.com.rinha.fraud_detection.dto.request.TerminalRequest;
import br.com.rinha.fraud_detection.dto.request.TransactionRequest;
import br.com.rinha.fraud_detection.dto.response.FraudScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudScoreServiceTest {

    @Mock
    private VectorIndexService vectorIndexService;

    private FraudScoreService fraudScoreService;

    @BeforeEach
    void setUp() {
        fraudScoreService = new FraudScoreService(vectorIndexService);
    }

    @Test
    void execute_oneFraudAmongFive() {
        when(vectorIndexService.search(any(float[].class), eq(5)))
            .thenReturn(List.of(
                vec("1", "legit"),
                vec("2", "legit"),
                vec("3", "fraud"),
                vec("4", "legit"),
                vec("5", "legit")
            ));

        FraudScoreResponse response = fraudScoreService.execute(minimalRequest());

        assertEquals(0.2f, response.fraudScore());
        assertTrue(response.approved());
    }

    @Test
    void execute_allFraud() {
        when(vectorIndexService.search(any(float[].class), eq(5)))
            .thenReturn(List.of(
                vec("1", "fraud"),
                vec("2", "fraud"),
                vec("3", "fraud"),
                vec("4", "fraud"),
                vec("5", "fraud")
            ));

        FraudScoreResponse response = fraudScoreService.execute(minimalRequest());

        assertEquals(1.0f, response.fraudScore());
        assertFalse(response.approved());
    }

    private static TransactionVector vec(String id, String label) {
        return new TransactionVector(id, new float[14], label);
    }

    private static FraudScoreRequest minimalRequest() {
        return new FraudScoreRequest(
            "tx-test",
            new TransactionRequest(100f, 1, Instant.parse("2024-06-01T14:00:00Z")),
            new CustomerRequest(50f, 1f, new String[] {}),
            new MerchantRequest("m1", "5411", 80f),
            new TerminalRequest(false, true, 10f),
            null
        );
    }
}
