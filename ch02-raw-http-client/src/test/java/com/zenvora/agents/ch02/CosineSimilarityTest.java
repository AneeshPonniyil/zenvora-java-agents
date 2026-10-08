package com.zenvora.agents.ch02;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CosineSimilarityTest {

    @Test
    void identicalDirectionIsOne() {
        assertEquals(1.0, CosineSimilarity.of(new double[] {1, 2, 3}, new double[] {2, 4, 6}), 1e-9);
    }

    @Test
    void perpendicularVectorsAreZero() {
        assertEquals(0.0, CosineSimilarity.of(new double[] {1, 0}, new double[] {0, 1}), 1e-9);
    }

    @Test
    void oppositeDirectionIsMinusOne() {
        assertEquals(-1.0, CosineSimilarity.of(new double[] {1, 1}, new double[] {-1, -1}), 1e-9);
    }

    @Test
    void rejectsDifferentDimensionsAndZeroVectors() {
        assertThrows(IllegalArgumentException.class,
                () -> CosineSimilarity.of(new double[] {1}, new double[] {1, 2}));
        assertThrows(IllegalArgumentException.class,
                () -> CosineSimilarity.of(new double[] {0, 0}, new double[] {1, 2}));
    }
}
