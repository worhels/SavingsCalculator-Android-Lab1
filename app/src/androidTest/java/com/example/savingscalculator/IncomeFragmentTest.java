package com.example.savingscalculator;

import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class IncomeFragmentTest {
    @Test public void validatesRestoresAndNavigatesBack() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.button_start).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container) instanceof IncomeFragment);
                activity.findViewById(R.id.button_income_next).performClick();
                TextInputLayout incomeLayout = activity.findViewById(R.id.layout_income);
                TextInputLayout shareLayout = activity.findViewById(R.id.layout_share);
                assertEquals(activity.getString(R.string.sc_error_required), incomeLayout.getError());
                assertEquals(activity.getString(R.string.sc_error_required), shareLayout.getError());
                EditText income = activity.findViewById(R.id.input_income);
                EditText share = activity.findViewById(R.id.input_share);
                income.setText("25000,50");
                for (String value : new String[]{"0", "1", "-0.25", "25"}) {
                    share.setText(value);
                    activity.findViewById(R.id.button_income_next).performClick();
                    assertEquals(activity.getString(R.string.sc_error_share), shareLayout.getError());
                }
                share.setText("0,25");
                activity.findViewById(R.id.button_income_next).performClick();
                assertNull(incomeLayout.getError());
                assertNull(shareLayout.getError());
                activity.getSupportFragmentManager().executePendingTransactions();
                CurrencyFragment currency = (CurrencyFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                assertEquals(25000.50, currency.requireArguments()
                        .getDouble(CurrencyFragment.ARG_MONTHLY_INCOME), 0.0);
                assertEquals(0.25, currency.requireArguments()
                        .getDouble(CurrencyFragment.ARG_INCOME_SHARE), 0.0);
                RadioGroup group = activity.findViewById(R.id.group_currency);
                assertEquals(R.id.radio_usd, group.getCheckedRadioButtonId());
                group.check(R.id.radio_eur);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                RadioGroup group = activity.findViewById(R.id.group_currency);
                assertEquals(R.id.radio_eur, group.getCheckedRadioButtonId());
                assertEquals(2, activity.getSupportFragmentManager().getBackStackEntryCount());
                CurrencyFragment currency = (CurrencyFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                assertEquals(25000.50, currency.requireArguments()
                        .getDouble(CurrencyFragment.ARG_MONTHLY_INCOME), 0.0);
                assertEquals(0.25, currency.requireArguments()
                        .getDouble(CurrencyFragment.ARG_INCOME_SHARE), 0.0);
                activity.findViewById(R.id.button_currency_back).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertEquals("25000,50", ((EditText) activity.findViewById(R.id.input_income)).getText().toString());
                assertEquals("0,25", ((EditText) activity.findViewById(R.id.input_share)).getText().toString());
                assertEquals(1, activity.getSupportFragmentManager().getBackStackEntryCount());
                assertTrue(activity.findViewById(R.id.button_income_next).isEnabled());
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals("25000,50", ((EditText) activity.findViewById(R.id.input_income)).getText().toString());
                assertEquals("0,25", ((EditText) activity.findViewById(R.id.input_share)).getText().toString());
                ((EditText) activity.findViewById(R.id.input_share)).setText("0.25");
                activity.findViewById(R.id.button_income_next).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
            });
            scenario.onActivity(activity -> {
                activity.getOnBackPressedDispatcher().onBackPressed();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container) instanceof IncomeFragment);
                assertEquals("0.25", ((EditText) activity.findViewById(R.id.input_share)).getText().toString());
                activity.findViewById(R.id.button_income_back).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container) instanceof HomeFragment);
                assertTrue(activity.findViewById(R.id.button_start).isEnabled());
                activity.findViewById(R.id.button_start).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
            });
            scenario.onActivity(activity -> {
                activity.getOnBackPressedDispatcher().onBackPressed();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container) instanceof HomeFragment);
                assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount());
            });
        }
    }
}
