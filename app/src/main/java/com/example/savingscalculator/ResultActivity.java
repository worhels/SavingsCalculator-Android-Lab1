package com.example.savingscalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity implements CalculationObserver {
    private String requestId;
    private String currency;
    private double income;
    private double share;
    private boolean observing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        requestId = getIntent().getStringExtra(SavingsCalculationService.EXTRA_REQUEST_ID);
        currency = getIntent().getStringExtra(SavingsCalculationService.EXTRA_CURRENCY);
        income = getIntent().getDoubleExtra(SavingsCalculationService.EXTRA_INCOME, Double.NaN);
        share = getIntent().getDoubleExtra(SavingsCalculationService.EXTRA_SHARE, Double.NaN);
        findViewById(R.id.button_result_ok).setOnClickListener(view -> returnHome());
        findViewById(R.id.button_result_retry).setOnClickListener(view -> startCalculationAgain());
        CalculationState current = CalculationRepository.getInstance().getCurrent();
        if (requestId != null && (current.requestId == null || !requestId.equals(current.requestId))) {
            startCalculationAgain();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        observing = true;
        CalculationRepository.getInstance().observe(this);
    }

    @Override
    protected void onStop() {
        observing = false;
        CalculationRepository.getInstance().removeObserver(this);
        super.onStop();
    }

    @Override
    public void onCalculationChanged(CalculationState state) {
        if (!observing || requestId == null || !requestId.equals(state.requestId)) return;
        ProgressBar progress = findViewById(R.id.result_progress);
        TextView status = findViewById(R.id.result_status);
        View table = findViewById(R.id.result_table);
        if (state.status == CalculationState.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            table.setVisibility(View.GONE);
            findViewById(R.id.button_result_retry).setVisibility(View.GONE);
            status.setText(R.string.sc_calculation_loading);
            status.setTextColor(ContextCompat.getColor(this, R.color.sc_accent));
        } else if (state.status == CalculationState.Status.ERROR) {
            progress.setVisibility(View.GONE);
            table.setVisibility(View.GONE);
            status.setText(R.string.sc_calculation_error);
            status.setTextColor(ContextCompat.getColor(this, R.color.sc_error));
            findViewById(R.id.button_result_retry).setVisibility(View.VISIBLE);
        } else if (state.status == CalculationState.Status.SUCCESS) {
            progress.setVisibility(View.GONE);
            table.setVisibility(View.VISIBLE);
            findViewById(R.id.button_result_retry).setVisibility(View.GONE);
            render(state);
        }
    }

    private void render(CalculationState state) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("uk-UA"));
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        SavingsCalculator.Result result = state.result;
        setText(R.id.result_currency, getString("USD".equals(state.currency)
                ? R.string.sc_currency_usd : R.string.sc_currency_eur));
        setText(R.id.result_income, hryvnias(format, state.monthlyIncome));
        setText(R.id.result_share, format.format(state.share));
        setText(R.id.result_start_rate, getString(R.string.sc_rate_value, format.format(state.startRate), state.currency));
        setText(R.id.result_end_rate, getString(R.string.sc_rate_value, format.format(state.endRate), state.currency));
        setText(R.id.result_sy, hryvnias(format, result.annualIncome));
        setText(R.id.result_sc, hryvnias(format, result.currencySpending));
        setText(R.id.result_w, getString(R.string.sc_currency_amount, format.format(result.purchasedCurrency), state.currency));
        setText(R.id.result_sh, hryvnias(format, result.currencyValue));
        setText(R.id.result_sl, hryvnias(format, result.remainingHryvnias));
        setText(R.id.result_h, hryvnias(format, result.total));
        setText(R.id.result_r, hryvnias(format, result.financialResult));
        double tolerance = Math.ulp(result.annualIncome) * 32;
        boolean positive = result.financialResult > tolerance;
        boolean negative = result.financialResult < -tolerance;
        TextView status = findViewById(R.id.result_status);
        status.setText(positive ? R.string.sc_result_profit
                : negative ? R.string.sc_result_loss : R.string.sc_result_unchanged);
        status.setTextColor(ContextCompat.getColor(this,
                negative ? R.color.sc_error : R.color.sc_accent));
    }

    private void startCalculationAgain() {
        findViewById(R.id.button_result_retry).setEnabled(false);
        CalculationRepository.getInstance().begin(requestId, currency, income, share);
        startService(new Intent(this, SavingsCalculationService.class)
                .putExtra(SavingsCalculationService.EXTRA_REQUEST_ID, requestId)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, income)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, share)
                .putExtra(SavingsCalculationService.EXTRA_CURRENCY, currency));
    }

    private void returnHome() {
        Intent home = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(home);
        finish();
    }

    private String hryvnias(NumberFormat format, double value) {
        return getString(R.string.sc_hryvnia_amount, format.format(Math.abs(value) < 0.005 ? 0 : value));
    }

    private void setText(int id, String value) {
        ((TextView) findViewById(id)).setText(value);
    }
}
