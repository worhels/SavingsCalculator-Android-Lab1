package com.example.savingscalculator;

import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StateRestorationTest {
    @Test public void restoresExplicitStateWithoutViewHierarchySaving() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(0, activity.getSupportFragmentManager().getBackStackEntryCount());
                assertTrue(current(activity) instanceof HomeFragment);
                assertTrue(activity.onStartCalculation());
                assertFalse(activity.onStartCalculation());
                activity.getSupportFragmentManager().executePendingTransactions();
                current(activity).requireView().setSaveFromParentEnabled(false);
                ((EditText) activity.findViewById(R.id.input_income)).setText("12345,67");
                ((EditText) activity.findViewById(R.id.input_share)).setText("0,");
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertTrue(current(activity) instanceof IncomeFragment);
                assertEquals("12345,67", text(activity, R.id.input_income));
                assertEquals("0,", text(activity, R.id.input_share));
                assertEquals(1, activity.getSupportFragmentManager().getBackStackEntryCount());
                ((EditText) activity.findViewById(R.id.input_share)).setText("0,25");
                current(activity).requireView().setSaveFromParentEnabled(false);
                activity.findViewById(R.id.button_income_next).performClick();
                activity.findViewById(R.id.button_income_next).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                assertEquals(2, activity.getSupportFragmentManager().getBackStackEntryCount());
                ((RadioGroup) activity.findViewById(R.id.group_currency)).check(R.id.radio_eur);
                current(activity).requireView().setSaveFromParentEnabled(false);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertTrue(current(activity) instanceof CurrencyFragment);
                assertEquals(R.id.radio_eur, ((RadioGroup) activity.findViewById(R.id.group_currency)).getCheckedRadioButtonId());
                activity.findViewById(R.id.button_currency_calculate).performClick();
                activity.getSupportFragmentManager().executePendingTransactions();
                current(activity).requireView().setSaveFromParentEnabled(false);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertTrue(current(activity) instanceof ResultFragment);
                assertEquals(3, activity.getSupportFragmentManager().getBackStackEntryCount());
                assertEquals(activity.getString(R.string.sc_currency_eur), text(activity, R.id.result_currency));
                assertEquals("0,25", text(activity, R.id.result_share));
                assertTrue(text(activity, R.id.result_income).contains("345,67"));
                assertTrue(activity.onNavigateBack());
                assertFalse(activity.onNavigateBack());
                activity.getSupportFragmentManager().executePendingTransactions();
                assertTrue(activity.onNavigateBack());
                activity.getSupportFragmentManager().executePendingTransactions();
                assertEquals("12345,67", text(activity, R.id.input_income));
                assertEquals("0,25", text(activity, R.id.input_share));
                ((EditText) activity.findViewById(R.id.input_income)).setText("999");
                current(activity).requireView().setSaveFromParentEnabled(false);
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals("999", text(activity, R.id.input_income));
                assertEquals("0,25", text(activity, R.id.input_share));
                assertEquals(1, activity.getSupportFragmentManager().getBackStackEntryCount());
            });
        }
    }

    private static Fragment current(MainActivity activity) {
        return activity.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    private static String text(MainActivity activity, int id) {
        return ((TextView) activity.findViewById(id)).getText().toString();
    }
}
