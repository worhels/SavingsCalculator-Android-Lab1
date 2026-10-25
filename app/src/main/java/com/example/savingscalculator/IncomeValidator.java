package com.example.savingscalculator;

import java.util.regex.Pattern;

final class IncomeValidator {
    enum Error { NONE, REQUIRED, INVALID_NUMBER, NON_POSITIVE_INCOME, INVALID_SHARE }

    private static final Pattern DECIMAL =
            Pattern.compile("[+-]?(?:[0-9]+(?:[.,][0-9]*)?|[.,][0-9]+)");

    private IncomeValidator() { }

    static Error validate(String input, boolean share) {
        String text = input.trim();
        if (text.isEmpty()) {
            return Error.REQUIRED;
        }
        if (!DECIMAL.matcher(text).matches()) {
            return Error.INVALID_NUMBER;
        }
        final double value;
        try {
            value = Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException exception) {
            return Error.INVALID_NUMBER;
        }
        if (!Double.isFinite(value)) {
            return Error.INVALID_NUMBER;
        }
        if (share) {
            return value > 0 && value < 1 ? Error.NONE : Error.INVALID_SHARE;
        }
        return value > 0 ? Error.NONE : Error.NON_POSITIVE_INCOME;
    }
}
