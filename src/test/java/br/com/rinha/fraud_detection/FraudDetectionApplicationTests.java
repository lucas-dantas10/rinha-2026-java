package br.com.rinha.fraud_detection;

import br.com.rinha.fraud_detection.service.VectorIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;

@SpringBootTest
class FraudDetectionApplicationTests {

    @MockitoBean
    private VectorIndexService vectorIndexService;

    @Test
    void contextLoads() {
        when(vectorIndexService.isReady()).thenReturn(true);
    }
}
