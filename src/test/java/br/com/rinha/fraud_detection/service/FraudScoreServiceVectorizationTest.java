package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.request.CustomerRequest;
import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.request.MerchantRequest;
import br.com.rinha.fraud_detection.dto.request.TerminalRequest;
import br.com.rinha.fraud_detection.dto.request.TransactionRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FraudScoreServiceVectorizationTest {

    @Mock
    private VectorIndexService vectorIndexService;

    @Test
    void fillQueryVector_legitExampleFromSpec() {
        FraudScoreService service = new FraudScoreService(vectorIndexService);

        FraudScoreRequest request = new FraudScoreRequest(
            "tx-1329056812",
            new TransactionRequest(41.12f, 2, Instant.parse("2026-03-11T18:45:53Z")),
            new CustomerRequest(82.24f, 3f, new String[] {"MERC-003", "MERC-016"}),
            new MerchantRequest("MERC-016", "5411", 60.25f),
            new TerminalRequest(false, true, 29.23f),
            null
        );

        float[] vector = new float[14];
        service.fillQueryVector(request, vector);

        float[] expected = {
            0.0041f, 0.1667f, 0.05f, 0.7826f, 0.3333f,
            -1f, -1f,
            0.0292f, 0.15f,
            0f, 1f, 0f, 0.15f, 0.006f
        };

        assertEquals(expected.length, vector.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], vector[i], 0.001f, "dimensão " + i);
        }
    }
}
