package com.example.savingscalculator;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.material.textfield.TextInputLayout;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class IncomeFragmentTest {
    @Test public void validatesRequiredFieldsAndRestoresInputOnRecreation() {
        try (ActivityScenario<IncomeActivity> scenario = ActivityScenario.launch(IncomeActivity.class)) {
            scenario.onActivity(activity -> {
                activity.findViewById(R.id.button_income_next).performClick();
                assertEquals(activity.getString(R.string.sc_error_required),
                        ((TextInputLayout) activity.findViewById(R.id.layout_income)).getError());
                assertEquals(activity.getString(R.string.sc_error_required),
                        ((TextInputLayout) activity.findViewById(R.id.layout_share)).getError());
                ((EditText) activity.findViewById(R.id.input_income)).setText("14500,75");
                ((EditText) activity.findViewById(R.id.input_share)).setText("0,");
            });
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals("14500,75", ((EditText) activity.findViewById(R.id.input_income)).getText().toString());
                assertEquals("0,", ((EditText) activity.findViewById(R.id.input_share)).getText().toString());
            });
        }
    }
}
