package com.example.savingscalculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class CurrencyActivity extends AppCompatActivity {
    private static final String STATE_CURRENCY = "selected_currency";
    private static final String EXTRA_REQUEST_ID = "request_id";
    private static final String EXTRA_CURRENCY = "currency";
    private double income;
    private double share;
    private String currency = "USD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);
        income = getIntent().getDoubleExtra(SavingsCalculationService.EXTRA_INCOME, Double.NaN);
        share = getIntent().getDoubleExtra(SavingsCalculationService.EXTRA_SHARE, Double.NaN);
        RadioGroup group = findViewById(R.id.group_currency);
        if (savedInstanceState != null) currency = savedInstanceState.getString(STATE_CURRENCY, "USD");
        group.check("EUR".equals(currency) ? R.id.radio_eur : R.id.radio_usd);
        group.setOnCheckedChangeListener((radioGroup, checkedId) ->
                currency = checkedId == R.id.radio_eur ? "EUR" : "USD");
        findViewById(R.id.button_currency_back).setOnClickListener(view -> finish());
        findViewById(R.id.button_currency_calculate).setOnClickListener(button -> {
            if (!button.isEnabled()) return;
            int selected = group.getCheckedRadioButtonId();
            if (selected != R.id.radio_usd && selected != R.id.radio_eur) {
                Toast.makeText(this, R.string.sc_currency_required, Toast.LENGTH_SHORT).show();
                return;
            }
            currency = selected == R.id.radio_usd ? "USD" : "EUR";
            String requestId = UUID.randomUUID().toString();
            CalculationRepository.getInstance().begin(requestId, currency, income, share);
            Intent service = new Intent(this, SavingsCalculationService.class)
                    .putExtra(SavingsCalculationService.EXTRA_REQUEST_ID, requestId)
                    .putExtra(SavingsCalculationService.EXTRA_INCOME, income)
                    .putExtra(SavingsCalculationService.EXTRA_SHARE, share)
                    .putExtra(SavingsCalculationService.EXTRA_CURRENCY, currency);
            startService(service);
            Intent result = new Intent(this, ResultActivity.class)
                    .putExtra(EXTRA_REQUEST_ID, requestId)
                    .putExtra(EXTRA_CURRENCY, currency)
                    .putExtra(SavingsCalculationService.EXTRA_INCOME, income)
                    .putExtra(SavingsCalculationService.EXTRA_SHARE, share);
            button.setEnabled(false);
            startActivity(result);
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_CURRENCY, currency);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.button_currency_calculate).setEnabled(true);
    }
}
