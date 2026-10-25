package com.example.savingscalculator;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class IncomeValidatorTest {
    @Test public void acceptsDecimalSeparators() {
        for (String value : new String[]{"25000", " 25000,50 ", "25000.50"}) {
            assertEquals(IncomeValidator.Error.NONE, IncomeValidator.validate(value, false));
        }
        for (String value : new String[]{"0.25", "0,25", ".5", "0.9999"}) {
            assertEquals(IncomeValidator.Error.NONE, IncomeValidator.validate(value, true));
        }
    }

    @Test public void rejectsEmptyAndMalformedNumbers() {
        for (boolean share : new boolean[]{false, true}) {
            assertEquals(IncomeValidator.Error.REQUIRED, IncomeValidator.validate("  ", share));
            for (String value : new String[]{"abc", "NaN", "Infinity", "1,2.3", "--2", ".", "1e3", "9".repeat(400)}) {
                assertEquals(value, IncomeValidator.Error.INVALID_NUMBER,
                        IncomeValidator.validate(value, share));
            }
        }
    }

    @Test public void enforcesStrictBounds() {
        for (String value : new String[]{"0", "-0", "-1.5"}) {
            assertEquals(IncomeValidator.Error.NON_POSITIVE_INCOME,
                    IncomeValidator.validate(value, false));
        }
        for (String value : new String[]{"0", "-0.25", "1", "1.0", "25"}) {
            assertEquals(IncomeValidator.Error.INVALID_SHARE, IncomeValidator.validate(value, true));
        }
    }
}
