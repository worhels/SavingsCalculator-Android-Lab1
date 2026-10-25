package com.example.savingscalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CurrencyFragment extends NavigationFragment {
    static final String ARG_MONTHLY_INCOME = "monthly_income";
    static final String ARG_INCOME_SHARE = "income_share";
    private static final String STATE_CURRENCY = "selected_currency";
    private double monthlyIncome;
    private double incomeShare;
    private String selectedCurrency = "USD";
    private boolean stateRestored;

    public CurrencyFragment() {
        super(R.layout.fragment_currency);
    }

    @NonNull
    public static CurrencyFragment newInstance(double monthlyIncome, double incomeShare) {
        Bundle arguments = new Bundle();
        arguments.putDouble(ARG_MONTHLY_INCOME, monthlyIncome);
        arguments.putDouble(ARG_INCOME_SHARE, incomeShare);
        CurrencyFragment fragment = new CurrencyFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = savedInstanceState != null ? savedInstanceState : requireArguments();
        monthlyIncome = arguments.getDouble(ARG_MONTHLY_INCOME);
        incomeShare = arguments.getDouble(ARG_INCOME_SHARE);
        if (savedInstanceState != null && !stateRestored) {
            selectedCurrency = savedInstanceState.getString(STATE_CURRENCY, "USD");
        }
        stateRestored = true;
        RadioGroup currencies = view.findViewById(R.id.group_currency);
        currencies.check("EUR".equals(selectedCurrency) ? R.id.radio_eur : R.id.radio_usd);
        currencies.setOnCheckedChangeListener((group, checkedId) ->
                selectedCurrency = checkedId == R.id.radio_eur ? "EUR" : "USD");
        view.findViewById(R.id.button_currency_calculate).setOnClickListener(v -> {
            if (!v.isEnabled()) return;
            int selectedId = currencies.getCheckedRadioButtonId();
            if (selectedId != R.id.radio_usd && selectedId != R.id.radio_eur) {
                Toast.makeText(requireContext(), R.string.sc_currency_required,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String currency = selectedId == R.id.radio_usd ? "USD" : "EUR";
            if (navigation().onCurrencySelected(monthlyIncome, incomeShare, currency)) {
                v.setEnabled(false);
            }
        });
        view.findViewById(R.id.button_currency_back).setOnClickListener(v -> {
            if (v.isEnabled() && navigation().onNavigateBack()) v.setEnabled(false);
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putDouble(ARG_MONTHLY_INCOME, monthlyIncome);
        outState.putDouble(ARG_INCOME_SHARE, incomeShare);
        outState.putString(STATE_CURRENCY, selectedCurrency);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        RadioGroup currencies = requireView().findViewById(R.id.group_currency);
        currencies.setOnCheckedChangeListener(null);
        super.onDestroyView();
    }
}
