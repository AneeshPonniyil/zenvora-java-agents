package com.zenvora.agents.ch02;

/** Cosine similarity between two embedding vectors: 1 = same direction, 0 = unrelated, -1 = opposite. */
public final class CosineSimilarity {

    private CosineSimilarity() {}

    public static double of(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            throw new IllegalArgumentException("Cannot compare a zero vector");
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
