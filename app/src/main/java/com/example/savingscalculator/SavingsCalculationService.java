package com.example.savingscalculator;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SavingsCalculationService extends IntentService {
    private static final String TAG = "SavingsCalcSvc";
    public static final String EXTRA_REQUEST_ID = "request_id";
    public static final String EXTRA_INCOME = "monthly_income";
    public static final String EXTRA_SHARE = "income_share";
    public static final String EXTRA_CURRENCY = "currency";

    public SavingsCalculationService() {
        super("SavingsCalculationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand startId=" + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent started");
        boolean success = false;
        String requestId = null;
        String currency = null;
        double income = Double.NaN;
        double share = Double.NaN;
        try (InputStreamReader source = new InputStreamReader(
                getAssets().open("exchange_rates.txt"), StandardCharsets.UTF_8)) {
            if (intent == null) throw new IllegalArgumentException("Missing calculation request");
            requestId = intent.getStringExtra(EXTRA_REQUEST_ID);
            currency = intent.getStringExtra(EXTRA_CURRENCY);
            income = intent.getDoubleExtra(EXTRA_INCOME, Double.NaN);
            share = intent.getDoubleExtra(EXTRA_SHARE, Double.NaN);
            if (requestId == null || currency == null) {
                throw new IllegalArgumentException("Incomplete calculation request");
            }
            ExchangeRates rates = ExchangeRates.read(source);
            DemoExchangeRates.Rates selected = rates.forCurrency(currency);
            SavingsCalculator.Result result = SavingsCalculator.calculate(
                    income, share, selected.start, selected.end);
            CalculationRepository.getInstance().succeed(CalculationState.success(
                    requestId, currency, income, share, selected.start, selected.end, result));
            success = true;
        } catch (Exception exception) {
            Log.e(TAG, "Calculation failed: " + exception.getClass().getSimpleName(), exception);
            String message = exception.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = getString(R.string.sc_calculation_error);
            }
            if (requestId != null && currency != null) {
                CalculationRepository.getInstance().fail(requestId, currency, income, share, message);
            }
        } finally {
            Log.d(TAG, "onHandleIntent finished; success=" + success);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
