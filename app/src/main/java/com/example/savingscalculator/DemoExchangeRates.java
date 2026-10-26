package com.example.savingscalculator;

public final class DemoExchangeRates {
    private DemoExchangeRates() { }

    public static final class Rates {
        public final double start;
        public final double end;

        Rates(double start, double end) {
            this.start = start;
            this.end = end;
        }
    }
}
