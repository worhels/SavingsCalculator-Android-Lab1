package com.example.savingscalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.NumberFormat;
import java.util.Locale;

public class ResultFragment extends NavigationFragment {
    private static final String ARG_INCOME = "monthly_income";
    private static final String ARG_SHARE = "income_share";
    private static final String ARG_CURRENCY = "currency";
    private double income;
    private double share;
    private String currency;

    public ResultFragment() {
        super(R.layout.fragment_result);
    }

    @NonNull
    public static ResultFragment newInstance(double income, double share, String currency) {
        Bundle arguments = new Bundle();
        arguments.putDouble(ARG_INCOME, income);
        arguments.putDouble(ARG_SHARE, share);
        arguments.putString(ARG_CURRENCY, currency);
        ResultFragment fragment = new ResultFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.button_result_ok).setOnClickListener(v -> {
            if (v.isEnabled() && navigation().onReturnHome()) v.setEnabled(false);
        });
        TextView status = view.findViewById(R.id.result_status);
        try {
            Bundle arguments = savedInstanceState != null ? savedInstanceState : requireArguments();
            income = arguments.getDouble(ARG_INCOME, Double.NaN);
            share = arguments.getDouble(ARG_SHARE, Double.NaN);
            currency = arguments.getString(ARG_CURRENCY);
            DemoExchangeRates.Rates rates = DemoExchangeRates.forCurrency(currency);
            SavingsCalculator.Result result = SavingsCalculator.calculate(income, share, rates.start, rates.end);
            NumberFormat format = NumberFormat.getNumberInstance(Locale.forLanguageTag("uk-UA"));
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            setText(view, R.id.result_currency, getString("USD".equals(currency)
                    ? R.string.sc_currency_usd : R.string.sc_currency_eur));
            setText(view, R.id.result_income, hryvnias(format, income));
            setText(view, R.id.result_share, format.format(share));
            setText(view, R.id.result_start_rate, getString(R.string.sc_rate_value, format.format(rates.start), currency));
            setText(view, R.id.result_end_rate, getString(R.string.sc_rate_value, format.format(rates.end), currency));
            setText(view, R.id.result_sy, hryvnias(format, result.annualIncome));
            setText(view, R.id.result_sc, hryvnias(format, result.currencySpending));
            setText(view, R.id.result_w, getString(R.string.sc_currency_amount, format.format(result.purchasedCurrency), currency));
            setText(view, R.id.result_sh, hryvnias(format, result.currencyValue));
            setText(view, R.id.result_sl, hryvnias(format, result.remainingHryvnias));
            setText(view, R.id.result_h, hryvnias(format, result.total));
            setText(view, R.id.result_r, hryvnias(format, result.financialResult));
            // Ignore floating-point cancellation noise only in the status, not the calculation.
            double tolerance = Math.ulp(result.annualIncome) * 32;
            boolean positive = result.financialResult > tolerance;
            boolean negative = result.financialResult < -tolerance;
            status.setText(positive ? R.string.sc_result_profit
                    : negative ? R.string.sc_result_loss : R.string.sc_result_unchanged);
            status.setTextColor(ContextCompat.getColor(requireContext(),
                    negative ? R.color.sc_error : R.color.sc_accent));
        } catch (IllegalArgumentException exception) {
            view.findViewById(R.id.result_table).setVisibility(View.GONE);
            status.setText(R.string.sc_result_invalid);
            status.setTextColor(ContextCompat.getColor(requireContext(), R.color.sc_error));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putDouble(ARG_INCOME, income);
        outState.putDouble(ARG_SHARE, share);
        outState.putString(ARG_CURRENCY, currency);
        super.onSaveInstanceState(outState);
    }

    private String hryvnias(NumberFormat format, double value) {
        // Prevent a rounded negative zero in the display only.
        return getString(R.string.sc_hryvnia_amount, format.format(Math.abs(value) < 0.005 ? 0 : value));
    }

    private static void setText(View view, int id, String text) {
        ((TextView) view.findViewById(id)).setText(text);
    }
}
