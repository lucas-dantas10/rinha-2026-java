package br.com.rinha.fraud_detection.dto;

import com.github.jelmerk.knn.Item;

public record TransactionVector(String id, float[] vector, String label)
    implements Item<String, float[]> {

    public static final int DIMENSIONS = 14;

    @Override public String id() { return id; }
    @Override public float[] vector() { return vector; }
    @Override public int dimensions() { return DIMENSIONS; }
}
