package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.request.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
public class FraudScoreService {

    private final static int MAX_AMOUNT = 1000;
    private final static int MAX_INSTALLMENTS = 12;
    private final static int AMOUNT_VS_AVG_RATIO = 10;
    private final static int MAX_MINUTES = 1440;
    private final static int MAX_KM = 1000;
    private final static int MAX_TX_COUNT_24H = 20;
    private final static int MAX_MERCHANT_AVG_AMOUNT = 1000;
    private final static float DEFAULT_VALUE_MCC_RISK = 0.5f;
    private final static Map<String, Float> mccRisk = new HashMap<>();

    private final float[] vector = new float[14];

    public FraudScoreService() {
        mccRisk.put("5411", 0.15f);
        mccRisk.put("5812", 0.30f);
        mccRisk.put("5912", 0.20f);
        mccRisk.put("5944", 0.45f);
        mccRisk.put("7801", 0.80f);
        mccRisk.put("7802", 0.75f);
        mccRisk.put("7995", 0.85f);
        mccRisk.put("4511", 0.35f);
        mccRisk.put("5311", 0.25f);
        mccRisk.put("5999", 0.50f);
    }

    public void execute(FraudScoreRequest request) {
        TransactionRequest transaction = request.transaction();
        CustomerRequest customer = request.customer();
        LastTransactionRequest lastTransaction = request.lastTransaction();
        TerminalRequest terminal = request.terminal();
        MerchantRequest merchant = request.merchant();

        int hourTransaction = transaction.requestedAt().atOffset(ZoneOffset.UTC).getHour();
        int dayOfWeekTransaction = transaction.requestedAt().atOffset(ZoneOffset.UTC).getDayOfWeek().getValue() - 1;

        int minutesLastTransaction;
        LocalDateTime dateTimeLastTransaction;
        float minutesSinceLastTx = -1;
        float kmFromLastTx = -1;

        if (lastTransaction != null) {
            dateTimeLastTransaction = lastTransaction.timestamp().atZone(ZoneId.systemDefault()).toLocalDateTime();
            minutesLastTransaction = dateTimeLastTransaction.getMinute();
            minutesSinceLastTx = limit((float) minutesLastTransaction / MAX_MINUTES);
            kmFromLastTx = limit(lastTransaction.kmFromCurrent() / MAX_KM);
        }

        boolean knowMerchant = false;

        for (String customerMerchant : customer.knownMerchant()) {
            if (customerMerchant.equals(merchant.id())) {
                knowMerchant = true;
                break;
            }
        }

        vector[0] = limit(transaction.amount() / MAX_AMOUNT);
        vector[1] = limit((float) transaction.installments() / MAX_INSTALLMENTS);
        vector[2] = limit((transaction.amount() / customer.avgAmount()) / AMOUNT_VS_AVG_RATIO);
        vector[3] = (float) hourTransaction / 23;
        vector[4] = (float) dayOfWeekTransaction / 6;
        vector[5] = minutesSinceLastTx;
        vector[6] = kmFromLastTx;
        vector[7] = limit(terminal.kmFromHome() / MAX_KM);
        vector[8] = limit(customer.txCount24h() / MAX_TX_COUNT_24H);
        vector[9] = terminal.isOnline() ? 1 : 0;
        vector[10] = terminal.cardPresent() ? 1 : 0;
        vector[11] = knowMerchant ? 0 : 1;
        vector[12] = mccRisk.getOrDefault(merchant.mcc(), DEFAULT_VALUE_MCC_RISK);
        vector[13] = limit(merchant.avgAmount() / MAX_MERCHANT_AVG_AMOUNT);
    }

    private float limit(float value) {
        if (value < 0.0) {
            return 0.0f;
        }

        if (value > 1.0) {
            return 1.0f;
        }

        return value;
    }
}
