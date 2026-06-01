package br.com.rinha.fraud_detection.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fraud.vector-index")
public record VectorIndexProperties(
    String path,
    int searchEf,
    int warmupIterations,
    boolean logSearchDuration
) {
    public VectorIndexProperties {
        if (path == null || path.isBlank()) {
            path = "references.idx";
        }
        if (searchEf <= 0) {
            searchEf = 40;
        }
        if (warmupIterations <= 0) {
            warmupIterations = 1000;
        }
    }
}
