package com.example.savingscalculator;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SavingsCalculationService extends IntentService {
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_INCOME = "monthly_income";
    public static final String EXTRA_SHARE = "income_share";
    public static final String EXTRA_CURRENCY = "currency";

    public SavingsCalculationService() {
        super("SavingsCalculationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String requestId = intent.getStringExtra(EXTRA_REQUEST_ID);
        String currency = intent.getStringExtra(EXTRA_CURRENCY);
        double income = intent.getDoubleExtra(EXTRA_INCOME, Double.NaN);
        double share = intent.getDoubleExtra(EXTRA_SHARE, Double.NaN);
        if (requestId == null || currency == null) return;
        try (InputStreamReader source = new InputStreamReader(
                getAssets().open("exchange_rates.txt"), StandardCharsets.UTF_8)) {
            ExchangeRates rates = ExchangeRates.read(source);
            DemoExchangeRates.Rates selected = rates.forCurrency(currency);
            SavingsCalculator.Result result = SavingsCalculator.calculate(
                    income, share, selected.start, selected.end);
            CalculationRepository.getInstance().succeed(CalculationState.success(
                    requestId, currency, income, share, selected.start, selected.end, result));
        } catch (Exception exception) {
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = getString(R.string.sc_calculation_error);
            }
            CalculationRepository.getInstance().fail(requestId, currency, income, share, message);
        }
    }
}
