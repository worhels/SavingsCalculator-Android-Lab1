package com.example.savingscalculator;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class IncomeFragment extends NavigationFragment {
    private static final String STATE_INCOME = "income_text";
    private static final String STATE_SHARE = "share_text";
    private String incomeText = "";
    private String shareText = "";
    private boolean stateRestored;

    @NonNull
    public static IncomeFragment newInstance() {
        return new IncomeFragment();
    }
    public IncomeFragment() {
        super(R.layout.fragment_income);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextInputEditText income = view.findViewById(R.id.input_income);
        TextInputEditText share = view.findViewById(R.id.input_share);
        TextInputLayout incomeLayout = view.findViewById(R.id.layout_income);
        TextInputLayout shareLayout = view.findViewById(R.id.layout_share);
        if (savedInstanceState != null && !stateRestored) {
            incomeText = savedInstanceState.getString(STATE_INCOME, "");
            shareText = savedInstanceState.getString(STATE_SHARE, "");
        }
        stateRestored = true;
        income.setText(incomeText);
        share.setText(shareText);

        view.findViewById(R.id.button_income_next).setOnClickListener(v -> {
            if (!v.isEnabled()) return;
            boolean incomeValid = validate(income, incomeLayout, false);
            boolean shareValid = validate(share, shareLayout, true);
            if (!incomeValid) {
                income.requestFocus();
            } else if (!shareValid) {
                share.requestFocus();
            } else {
                double monthlyIncome = Double.parseDouble(
                        income.getText().toString().trim().replace(',', '.'));
                double incomeShare = Double.parseDouble(
                        share.getText().toString().trim().replace(',', '.'));
                if (navigation().onIncomeSubmitted(monthlyIncome, incomeShare)) {
                    v.setEnabled(false);
                    WindowCompat.getInsetsController(requireActivity().getWindow(), view)
                            .hide(WindowInsetsCompat.Type.ime());
                }
            }
        });
        view.findViewById(R.id.button_income_back).setOnClickListener(v -> {
            if (v.isEnabled() && navigation().onNavigateBack()) v.setEnabled(false);
        });
    }

    private void captureInput() {
        View view = getView();
        if (view == null) return;
        TextInputEditText income = view.findViewById(R.id.input_income);
        TextInputEditText share = view.findViewById(R.id.input_share);
        incomeText = income.getText() == null ? "" : income.getText().toString();
        shareText = share.getText() == null ? "" : share.getText().toString();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        captureInput();
        outState.putString(STATE_INCOME, incomeText);
        outState.putString(STATE_SHARE, shareText);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        captureInput();
        super.onDestroyView();
    }

    private boolean validate(TextInputEditText input, TextInputLayout layout, boolean share) {
        String text = input.getText() == null ? "" : input.getText().toString();
        IncomeValidator.Error error = IncomeValidator.validate(text, share);
        int message;
        switch (error) {
            case REQUIRED:
                message = R.string.sc_error_required;
                break;
            case INVALID_NUMBER:
                message = R.string.sc_error_number;
                break;
            case NON_POSITIVE_INCOME:
                message = R.string.sc_error_income;
                break;
            case INVALID_SHARE:
                message = R.string.sc_error_share;
                break;
            default:
                layout.setError(null);
                return true;
        }
        layout.setError(getString(message));
        return false;
    }
}
