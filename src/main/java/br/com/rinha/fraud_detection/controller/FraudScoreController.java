package br.com.rinha.fraud_detection.controller;

import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.response.FraudScoreResponse;
import br.com.rinha.fraud_detection.service.FraudScoreService;
import br.com.rinha.fraud_detection.service.VectorIndexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fraud-score")
public class FraudScoreController {

    private final FraudScoreService fraudScoreService;
    private final VectorIndexService vectorIndexService;

    public FraudScoreController(
        FraudScoreService fraudScoreService,
        VectorIndexService vectorIndexService
    ) {
        this.fraudScoreService = fraudScoreService;
        this.vectorIndexService = vectorIndexService;
    }

    @PostMapping
    public ResponseEntity<FraudScoreResponse> execute(
        @RequestBody FraudScoreRequest request
    ) {
        if (!vectorIndexService.isReady()) {
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .build();
        }
        return ResponseEntity.ok(fraudScoreService.execute(request));
    }
}
