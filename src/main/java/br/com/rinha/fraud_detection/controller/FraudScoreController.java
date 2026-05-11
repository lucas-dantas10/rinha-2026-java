package br.com.rinha.fraud_detection.controller;

import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.response.FraudScoreResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fraud-score")
public class FraudScoreController {

    @PostMapping
    public ResponseEntity<FraudScoreResponse> execute(
        @RequestBody FraudScoreRequest request) {
        return ResponseEntity.ok(new FraudScoreResponse(false, 1.0f));
    }
}
