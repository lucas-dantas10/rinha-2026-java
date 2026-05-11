package br.com.rinha.fraud_detection.controller;

import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/fraud-score")
public class FraudScoreController {

    @PostMapping
    public void execute(@RequestBody FraudScoreRequest request) {

    }
}
