package com.example.savingscalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class IncomeActivity extends AppCompatActivity {
    private static final String STATE_INCOME = "income_text";
    private static final String STATE_SHARE = "share_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);
        TextInputEditText income = findViewById(R.id.input_income);
        TextInputEditText share = findViewById(R.id.input_share);
        if (savedInstanceState != null) {
            income.setText(savedInstanceState.getString(STATE_INCOME, ""));
            share.setText(savedInstanceState.getString(STATE_SHARE, ""));
        }
        findViewById(R.id.button_income_next).setOnClickListener(button -> {
            if (!button.isEnabled()) return;
            boolean incomeValid = validate(income, findViewById(R.id.layout_income), false);
            boolean shareValid = validate(share, findViewById(R.id.layout_share), true);
            if (!incomeValid) {
                income.requestFocus();
                return;
            }
            if (!shareValid) {
                share.requestFocus();
                return;
            }
            Intent next = new Intent(this, CurrencyActivity.class)
                    .putExtra(SavingsCalculationService.EXTRA_INCOME, parse(income))
                    .putExtra(SavingsCalculationService.EXTRA_SHARE, parse(share));
            button.setEnabled(false);
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                    .hide(WindowInsetsCompat.Type.ime());
            startActivity(next);
        });
        findViewById(R.id.button_income_back).setOnClickListener(view -> finish());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_INCOME, text((TextInputEditText) findViewById(R.id.input_income)));
        outState.putString(STATE_SHARE, text((TextInputEditText) findViewById(R.id.input_share)));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.button_income_next).setEnabled(true);
    }

    private boolean validate(TextInputEditText input, TextInputLayout layout, boolean isShare) {
        IncomeValidator.Error error = IncomeValidator.validate(text(input), isShare);
        int message;
        switch (error) {
            case REQUIRED: message = R.string.sc_error_required; break;
            case INVALID_NUMBER: message = R.string.sc_error_number; break;
            case NON_POSITIVE_INCOME: message = R.string.sc_error_income; break;
            case INVALID_SHARE: message = R.string.sc_error_share; break;
            default:
                layout.setError(null);
                return true;
        }
        layout.setError(getString(message));
        return false;
    }

    private static double parse(TextInputEditText input) {
        return Double.parseDouble(text(input).trim().replace(',', '.'));
    }

    private static String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString();
    }
}
