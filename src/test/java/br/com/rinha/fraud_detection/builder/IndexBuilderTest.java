package br.com.rinha.fraud_detection.builder;

import br.com.rinha.fraud_detection.dto.TransactionVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndexBuilderTest {

    @Test
    void isValidVector_accepts14Dimensions() {
        assertTrue(IndexBuilder.isValidVector(
            new TransactionVector("1", new float[14], "legit")
        ));
    }

    @Test
    void isValidVector_rejectsWrongSize() {
        assertFalse(IndexBuilder.isValidVector(
            new TransactionVector("1", new float[13], "legit")
        ));
    }

    @Test
    void capacityFor_addsTenPercentMargin() {
        assertEquals(11, IndexBuilder.capacityFor(10));
    }
}
