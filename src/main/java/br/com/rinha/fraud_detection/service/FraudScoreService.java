package br.com.rinha.fraud_detection.service;

import br.com.rinha.fraud_detection.dto.TransactionVector;
import br.com.rinha.fraud_detection.dto.request.*;
import br.com.rinha.fraud_detection.dto.response.FraudScoreResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FraudScoreService {

    private static final int MAX_AMOUNT = 10_000;
    private static final int MAX_INSTALLMENTS = 12;
    private static final int AMOUNT_VS_AVG_RATIO = 10;
    private static final int MAX_MINUTES = 1440;
    private static final int MAX_KM = 1000;
    private static final int MAX_TX_COUNT_24H = 20;
    private static final int MAX_MERCHANT_AVG_AMOUNT = 10_000;
    private static final float DEFAULT_VALUE_MCC_RISK = 0.5f;
    private static final int K = 5;
    private static final float THRESHOLD = 0.6f;

    private static final Map<String, Float> MCC_RISK = new HashMap<>();

    static {
        MCC_RISK.put("5411", 0.15f);
        MCC_RISK.put("5812", 0.30f);
        MCC_RISK.put("5912", 0.20f);
        MCC_RISK.put("5944", 0.45f);
        MCC_RISK.put("7801", 0.80f);
        MCC_RISK.put("7802", 0.75f);
        MCC_RISK.put("7995", 0.85f);
        MCC_RISK.put("4511", 0.35f);
        MCC_RISK.put("5311", 0.25f);
        MCC_RISK.put("5999", 0.50f);
    }

    private final VectorIndexService vectorIndexService;
    private final ThreadLocal<float[]> queryBuffer =
        ThreadLocal.withInitial(() -> new float[TransactionVector.DIMENSIONS]);

    public FraudScoreService(VectorIndexService vectorIndexService) {
        this.vectorIndexService = vectorIndexService;
    }

    public FraudScoreResponse execute(FraudScoreRequest request) {
        float[] queryVector = queryBuffer.get();
        fillQueryVector(request, queryVector);

        List<TransactionVector> neighbors = vectorIndexService.search(queryVector, K);

        int fraudCount = 0;

        for (TransactionVector neighbor : neighbors) {
            if ("fraud".equals(neighbor.label())) {
                fraudCount++;
            }
        }

        float fraudScore = (float) fraudCount / K;

        return new FraudScoreResponse(fraudScore < THRESHOLD, fraudScore);
    }

    void fillQueryVector(FraudScoreRequest request, float[] vector) {
        TransactionRequest transaction = request.transaction();
        CustomerRequest customer = request.customer();
        LastTransactionRequest lastTransaction = request.lastTransaction();
        TerminalRequest terminal = request.terminal();
        MerchantRequest merchant = request.merchant();

        int hourTransaction = transaction.requestedAt().atOffset(ZoneOffset.UTC).getHour();
        int dayOfWeekTransaction =
            transaction.requestedAt().atOffset(ZoneOffset.UTC).getDayOfWeek().getValue() - 1;

        float minutesSinceLastTx = -1f;
        float kmFromLastTx = -1f;

        if (lastTransaction != null) {
            long minutes = Duration.between(
                lastTransaction.timestamp(),
                transaction.requestedAt()
            ).toMinutes();
            minutesSinceLastTx = limit((float) minutes / MAX_MINUTES);
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
        vector[9] = terminal.isOnline() ? 1f : 0f;
        vector[10] = terminal.cardPresent() ? 1f : 0f;
        vector[11] = knowMerchant ? 0f : 1f;
        vector[12] = MCC_RISK.getOrDefault(merchant.mcc(), DEFAULT_VALUE_MCC_RISK);
        vector[13] = limit(merchant.avgAmount() / MAX_MERCHANT_AVG_AMOUNT);
    }

    private static float limit(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }
}
