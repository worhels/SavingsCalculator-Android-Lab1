package com.example.savingscalculator;

/** Demonstration values only; not official or bank exchange rates. */
public final class DemoExchangeRates {
    public static final Rates USD = new Rates(40.00, 42.00);
    public static final Rates EUR = new Rates(44.00, 46.00);

    private DemoExchangeRates() { }

    public static Rates forCurrency(String code) {
        if ("USD".equals(code)) return USD;
        if ("EUR".equals(code)) return EUR;
        throw new IllegalArgumentException("Unsupported currency: " + code);
    }

    public static final class Rates {
        public final double start;
        public final double end;

        private Rates(double start, double end) {
            this.start = start;
            this.end = end;
        }
    }
}
