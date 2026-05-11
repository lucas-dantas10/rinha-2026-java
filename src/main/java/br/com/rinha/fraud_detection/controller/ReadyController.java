package br.com.rinha.fraud_detection.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ready")
public class ReadyController {

    @GetMapping
    public ResponseEntity<String> execute() {
        return ResponseEntity.ok("Ok");
    }
}
