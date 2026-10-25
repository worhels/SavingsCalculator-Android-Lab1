package com.example.savingscalculator;

/** Pure calculations for laboratory 1, variant 14. No intermediate rounding. */
public final class SavingsCalculator {
    private SavingsCalculator() { }

    public static Result calculate(double monthlyIncome, double share,
                                   double startRate, double endRate) {
        requirePositiveFinite(monthlyIncome, "M");
        requirePositiveFinite(startRate, "C_START");
        requirePositiveFinite(endRate, "C_END");
        if (!Double.isFinite(share) || share <= 0 || share >= 1) {
            throw new IllegalArgumentException("p must be finite and strictly between 0 and 1");
        }
        double annualIncome = 12 * monthlyIncome; // SY
        double currencySpending = share * annualIncome; // Sc
        double purchasedCurrency = 0;
        for (int month = 1; month <= 12; month++) {
            // Ci = C_START + i * (C_END - C_START) / 12.
            // Multiply by the fraction first to avoid intermediate overflow.
            double rate = startRate + (endRate - startRate) * (month / 12.0);
            if (month == 12) {
                rate = endRate;
            }
            requirePositiveFinite(rate, "Ci");
            purchasedCurrency += share * monthlyIncome / rate; // W
        }
        double currencyValue = purchasedCurrency * endRate; // SH
        double remainingHryvnias = annualIncome - currencySpending; // SL
        double total = currencyValue + remainingHryvnias; // H
        double financialResult = total - annualIncome; // R
        for (double value : new double[]{annualIncome, currencySpending, purchasedCurrency,
                currencyValue, remainingHryvnias, total, financialResult}) {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Calculation exceeds the supported numeric range");
            }
        }
        return new Result(annualIncome, currencySpending, purchasedCurrency,
                currencyValue, remainingHryvnias, total, financialResult);
    }

    private static void requirePositiveFinite(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive and finite");
        }
    }

    public static final class Result {
        public final double annualIncome;
        public final double currencySpending;
        public final double purchasedCurrency;
        public final double currencyValue;
        public final double remainingHryvnias;
        public final double total;
        public final double financialResult;

        private Result(double annualIncome, double currencySpending, double purchasedCurrency,
                       double currencyValue, double remainingHryvnias, double total,
                       double financialResult) {
            this.annualIncome = annualIncome;
            this.currencySpending = currencySpending;
            this.purchasedCurrency = purchasedCurrency;
            this.currencyValue = currencyValue;
            this.remainingHryvnias = remainingHryvnias;
            this.total = total;
            this.financialResult = financialResult;
        }
    }
}
