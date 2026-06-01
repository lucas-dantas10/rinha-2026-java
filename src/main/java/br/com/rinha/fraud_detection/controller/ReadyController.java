package br.com.rinha.fraud_detection.controller;

import br.com.rinha.fraud_detection.service.VectorIndexService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ready")
public class ReadyController {

    private final VectorIndexService vectorIndexService;

    public ReadyController(VectorIndexService vectorIndexService) {
        this.vectorIndexService = vectorIndexService;
    }

    @GetMapping
    public ResponseEntity<String> execute() {
        if (!vectorIndexService.isReady()) {
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Índice HNSW ainda não está pronto");
        }
        return ResponseEntity.ok("Ok");
    }
}
