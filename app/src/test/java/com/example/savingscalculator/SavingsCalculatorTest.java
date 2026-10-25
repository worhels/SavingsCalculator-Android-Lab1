package com.example.savingscalculator;

import org.junit.Test;
import static org.junit.Assert.*;

public class SavingsCalculatorTest {
    @Test public void constantRateHasNoGain() {
        SavingsCalculator.Result result = SavingsCalculator.calculate(10000, 0.5, 40, 40);
        assertEquals(1500, result.purchasedCurrency, 1e-9);
        assertEquals(120000, result.total, 1e-8);
        assertEquals(0, result.financialResult, 1e-8);
    }

    @Test public void risingRateProducesProfit() {
        assertTrue(SavingsCalculator.calculate(10000, 0.5, 40, 42).financialResult > 0);
    }

    @Test public void fallingRateProducesLoss() {
        assertTrue(SavingsCalculator.calculate(10000, 0.5, 42, 40).financialResult < 0);
    }

    @Test public void computesAllTwelvePurchasesAndTotals() {
        SavingsCalculator.Result result = SavingsCalculator.calculate(10000, 0.5, 40, 52);

        double expectedCurrency = 1297.5045588053658;
        assertEquals(120000, result.annualIncome, 0);
        assertEquals(60000, result.currencySpending, 0);
        assertEquals(60000, result.remainingHryvnias, 0);
        assertEquals(expectedCurrency, result.purchasedCurrency, 1e-10);
        assertEquals(expectedCurrency * 52, result.currencyValue, 1e-8);
        assertEquals(expectedCurrency * 52 + 60000, result.total, 1e-8);
        assertEquals(expectedCurrency * 52 - 60000, result.financialResult, 1e-8);
        assertNotEquals(60000 / 46.0, result.purchasedCurrency, 1e-5);
    }

    @Test public void rejectsInvalidInputs() {
        for (double value : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> SavingsCalculator.calculate(value, 0.5, 40, 42));
            assertThrows(IllegalArgumentException.class, () -> SavingsCalculator.calculate(10000, 0.5, value, 42));
            assertThrows(IllegalArgumentException.class, () -> SavingsCalculator.calculate(10000, 0.5, 40, value));
        }
        for (double share : new double[]{0, 1, -0.1, 1.1, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> SavingsCalculator.calculate(10000, share, 40, 42));
        }
    }

    @Test public void rejectsOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> SavingsCalculator.calculate(Double.MAX_VALUE, 0.5, 40, 42));
    }

    @Test public void usesExplicitDemoRates() {
        assertEquals(40, DemoExchangeRates.forCurrency("USD").start, 0);
        assertEquals(42, DemoExchangeRates.forCurrency("USD").end, 0);
        assertEquals(44, DemoExchangeRates.forCurrency("EUR").start, 0);
        assertEquals(46, DemoExchangeRates.forCurrency("EUR").end, 0);
        assertThrows(IllegalArgumentException.class, () -> DemoExchangeRates.forCurrency("GBP"));
    }
}
