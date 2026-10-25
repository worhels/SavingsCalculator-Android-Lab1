package com.example.savingscalculator;

import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ResultFragmentTest {
    @Test public void restoresResultsAndClearsBackStack() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            String[] displayedResult = new String[1];
            scenario.onActivity(activity -> {
                FragmentManager manager = activity.getSupportFragmentManager();
                activity.findViewById(R.id.button_start).performClick();
                manager.executePendingTransactions();
                ((EditText) activity.findViewById(R.id.input_income)).setText("10000");
                ((EditText) activity.findViewById(R.id.input_share)).setText("0,50");
                activity.findViewById(R.id.button_income_next).performClick();
                manager.executePendingTransactions();
                ((RadioGroup) activity.findViewById(R.id.group_currency)).check(R.id.radio_eur);
                View calculate = activity.findViewById(R.id.button_currency_calculate);
                calculate.performClick();
                calculate.performClick();
                manager.executePendingTransactions();
                assertEquals(3, manager.getBackStackEntryCount());
                assertTrue(manager.findFragmentById(R.id.fragment_container) instanceof ResultFragment);
                assertEquals(activity.getString(R.string.sc_currency_eur),
                        ((TextView) activity.findViewById(R.id.result_currency)).getText().toString());
                assertEquals(activity.getString(R.string.sc_result_profit),
                        ((TextView) activity.findViewById(R.id.result_status)).getText().toString());
                displayedResult[0] = ((TextView) activity.findViewById(R.id.result_r)).getText().toString();
                assertTrue(displayedResult[0].contains(","));
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(displayedResult[0], ((TextView) activity.findViewById(R.id.result_r)).getText().toString());
                activity.getOnBackPressedDispatcher().onBackPressed();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                        instanceof CurrencyFragment);
                assertEquals(R.id.radio_eur, ((RadioGroup) activity.findViewById(R.id.group_currency)).getCheckedRadioButtonId());
                assertTrue(activity.findViewById(R.id.button_currency_calculate).isEnabled());
                activity.findViewById(R.id.button_currency_calculate).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                activity.findViewById(R.id.button_result_ok).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount());
                assertTrue(activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                        instanceof HomeFragment);
                assertTrue(activity.findViewById(R.id.button_start).isEnabled());
            });
        }
    }
}
