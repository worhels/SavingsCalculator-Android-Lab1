package com.example.savingscalculator;

import android.content.Intent;
import android.widget.RadioGroup;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class StateRestorationTest {
    @Test public void currencyChoiceAndIncomeParametersSurviveActivityRecreation() {
        Intent intent = new Intent(InstrumentationRegistryProvider.context(), CurrencyActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, 12345.67)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, 0.25);
        try (ActivityScenario<CurrencyActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> ((RadioGroup) activity.findViewById(R.id.group_currency))
                    .check(R.id.radio_eur));
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals(R.id.radio_eur,
                        ((RadioGroup) activity.findViewById(R.id.group_currency)).getCheckedRadioButtonId());
                assertEquals(12345.67, activity.getIntent()
                        .getDoubleExtra(SavingsCalculationService.EXTRA_INCOME, 0), 0);
                assertEquals(0.25, activity.getIntent()
                        .getDoubleExtra(SavingsCalculationService.EXTRA_SHARE, 0), 0);
            });
        }
    }
}
