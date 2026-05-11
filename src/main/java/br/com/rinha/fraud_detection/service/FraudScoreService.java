package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.request.CustomerRequest;
import br.com.rinha.fraud_detection.dto.request.FraudScoreRequest;
import br.com.rinha.fraud_detection.dto.request.TransactionRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FraudScoreService {

    private final static int MAX_AMOUNT = 1000;
    private final static int MAX_INSTALLMENTS = 12;
    private final static int AMOUNT_VS_AVG_RATIO = 10;
    private final static int MAX_MINUTES = 1440;
    private final static int MAX_KM = 1000;
    private final static int MAX_TX_COUNT_24H = 20;
    private final static int MAX_MERCHANT_AVG_AMOUNT = 1000;

    private float[] vector;

    public void execute(FraudScoreRequest request) {
        TransactionRequest transaction = request.transaction();
        CustomerRequest customer = request.customer();

        DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime dateTime = LocalDateTime.parse(transaction.requestedAt(), dateTimeFormatter);

        int hour = dateTime.getHour();

        vector[0] = limit(transaction.amount() / MAX_AMOUNT);
        vector[1] = limit((float) transaction.installments() / MAX_INSTALLMENTS);
        vector[2] = limit((transaction.amount() / customer.avgAmount()) / AMOUNT_VS_AVG_RATIO);
        vector[3] = (float) hour / 23;
    }

    private float limit(float value) {
        if (value < 0.0) {
            return 0.0f;
        } else if (value > 1.0) {
            return 1.0f;
        } else {
            return value;
        }
    }
}
