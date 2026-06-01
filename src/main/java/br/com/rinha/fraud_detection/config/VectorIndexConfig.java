package br.com.rinha.fraud_detection.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VectorIndexProperties.class)
public class VectorIndexConfig {
}
