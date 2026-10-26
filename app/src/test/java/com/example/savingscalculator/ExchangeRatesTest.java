package com.example.savingscalculator;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ExchangeRatesTest {
    @Test public void readsUsdAndEurFromText() throws Exception {
        ExchangeRates rates = ExchangeRates.read(new StringReader(
                "# rates\nUSD,40.00,42.00\nEUR;44.00;46.00\n"));
        assertEquals(40, rates.forCurrency("USD").start, 0);
        assertEquals(42, rates.forCurrency("USD").end, 0);
        assertEquals(44, rates.forCurrency("EUR").start, 0);
        assertEquals(46, rates.forCurrency("EUR").end, 0);
    }

    @Test public void acceptsUtf8BomAndWhitespaceDelimitedRows() throws Exception {
        ExchangeRates rates = ExchangeRates.read(new StringReader(
                "\uFEFF# rates\nUSD 40 42\nEUR 44 46\n"));
        assertEquals(42, rates.forCurrency("usd").end, 0);
        assertEquals(46, rates.forCurrency("eur").end, 0);
    }

    @Test public void rejectsMissingDuplicateMalformedAndInvalidRates() {
        for (String input : new String[]{"USD,40,42\n", "USD,40,42\nUSD,41,43\nEUR,44,46\n",
                "USD,0,42\nEUR,44,46\n", "USD,NaN,42\nEUR,44,46\n",
                "USD,40\nEUR,44,46\n", "GBP,1,2\nUSD,40,42\nEUR,44,46\n"}) {
            assertThrows(input, IOException.class,
                    () -> ExchangeRates.read(new StringReader(input)));
        }
    }

    @Test public void rejectsUnknownCurrency() throws Exception {
        ExchangeRates rates = ExchangeRates.read(new StringReader("USD,40,42\nEUR,44,46\n"));
        assertThrows(IOException.class, () -> rates.forCurrency("GBP"));
    }
}
