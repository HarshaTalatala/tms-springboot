package com.harsha.tms.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class WeightUnitTest {

    @Test
    void testWeightUnitValues() {
        WeightUnit[] values = WeightUnit.values();
        assertEquals(2, values.length);
        assertEquals(WeightUnit.KG, values[0]);
        assertEquals(WeightUnit.TON, values[1]);
    }

    @Test
    void testWeightUnitValueOf() {
        assertEquals(WeightUnit.KG, WeightUnit.valueOf("KG"));
        assertEquals(WeightUnit.TON, WeightUnit.valueOf("TON"));
    }
}
