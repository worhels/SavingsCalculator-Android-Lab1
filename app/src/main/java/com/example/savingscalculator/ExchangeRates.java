package com.example.savingscalculator;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public final class ExchangeRates {
    private final Map<String, DemoExchangeRates.Rates> rates;

    private ExchangeRates(Map<String, DemoExchangeRates.Rates> rates) {
        this.rates = rates;
    }

    @NonNull
    public static ExchangeRates read(@NonNull Reader source) throws IOException {
        Map<String, DemoExchangeRates.Rates> parsed = new HashMap<>();
        BufferedReader reader = source instanceof BufferedReader
                ? (BufferedReader) source : new BufferedReader(source);
        String line;
        int lineNumber = 0;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String value = line.trim();
            if (lineNumber == 1 && value.startsWith("\uFEFF")) value = value.substring(1).trim();
            if (value.isEmpty() || value.startsWith("#")) continue;
            String[] columns = value.split("\\s*[,;]\\s*|\\s+");
            if (columns.length != 3) {
                throw new IOException("Invalid exchange-rate line " + lineNumber);
            }
            String currency = columns[0].toUpperCase(java.util.Locale.ROOT);
            if (!"USD".equals(currency) && !"EUR".equals(currency)) {
                throw new IOException("Unsupported currency on line " + lineNumber);
            }
            if (parsed.containsKey(currency)) {
                throw new IOException("Duplicate currency on line " + lineNumber);
            }
            try {
                double start = Double.parseDouble(columns[1]);
                double end = Double.parseDouble(columns[2]);
                if (!Double.isFinite(start) || start <= 0 || !Double.isFinite(end) || end <= 0) {
                    throw new NumberFormatException();
                }
                parsed.put(currency, new DemoExchangeRates.Rates(start, end));
            } catch (NumberFormatException exception) {
                throw new IOException("Invalid exchange rate on line " + lineNumber, exception);
            }
        }
        if (!parsed.containsKey("USD") || !parsed.containsKey("EUR")) {
            throw new IOException("Exchange-rate file must contain USD and EUR");
        }
        return new ExchangeRates(parsed);
    }

    @NonNull
    public DemoExchangeRates.Rates forCurrency(@NonNull String currency) throws IOException {
        DemoExchangeRates.Rates result = rates.get(currency.toUpperCase(java.util.Locale.ROOT));
        if (result == null) throw new IOException("Unsupported currency: " + currency);
        return result;
    }
}
