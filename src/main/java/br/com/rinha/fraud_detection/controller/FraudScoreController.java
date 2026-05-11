package br.com.rinha.fraud_detection.controller;

import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.response.FraudScoreResponse;
import br.com.rinha.fraud_detection.service.FraudScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fraud-score")
public class FraudScoreController {

    private final FraudScoreService fraudScoreService;

    public FraudScoreController(FraudScoreService fraudScoreService) {
        this.fraudScoreService = fraudScoreService;
    }

    @PostMapping
    public ResponseEntity<FraudScoreResponse> execute(
        @RequestBody FraudScoreRequest request) {
        fraudScoreService.execute(request);

        return ResponseEntity.ok(new FraudScoreResponse(false, 1.0f));
    }
}
