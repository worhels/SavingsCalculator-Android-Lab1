package com.example.savingscalculator;

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
        double annualIncome = 12 * monthlyIncome;
        double currencySpending = share * annualIncome;
        double purchasedCurrency = 0;
        for (int month = 1; month <= 12; month++) {

            double rate = startRate + (endRate - startRate) * (month / 12.0);
            if (month == 12) {
                rate = endRate;
            }
            requirePositiveFinite(rate, "Ci");
            purchasedCurrency += share * monthlyIncome / rate;
        }
        double currencyValue = purchasedCurrency * endRate;
        double remainingHryvnias = annualIncome - currencySpending;
        double total = currencyValue + remainingHryvnias;
        double financialResult = total - annualIncome;
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
