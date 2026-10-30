package com.example.savingscalculator;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Rect;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ConstraintLayoutOrientationTest {
    @Test
    public void homeIncomeCurrencyAndResultRemainUsableAfterRotation() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            rotate(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    Configuration.ORIENTATION_LANDSCAPE);
            scenario.onActivity(activity -> {
                assertVerticalOrder(activity, R.id.home_logo, R.id.home_title);
                assertVerticalOrder(activity, R.id.button_start, R.id.button_exit);
            });
        }

        try (ActivityScenario<IncomeActivity> scenario = ActivityScenario.launch(IncomeActivity.class)) {
            scenario.onActivity(activity -> {
                ((com.google.android.material.textfield.TextInputEditText)
                        activity.findViewById(R.id.input_income)).setText("10000");
                ((com.google.android.material.textfield.TextInputEditText)
                        activity.findViewById(R.id.input_share)).setText("0.5");
            });
            rotate(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    Configuration.ORIENTATION_LANDSCAPE);
            scenario.onActivity(activity -> {
                assertEquals("10000", ((com.google.android.material.textfield.TextInputEditText)
                        activity.findViewById(R.id.input_income)).getText().toString());
                assertEquals("0.5", ((com.google.android.material.textfield.TextInputEditText)
                        activity.findViewById(R.id.input_share)).getText().toString());
                assertVerticalOrder(activity, R.id.layout_income, R.id.layout_share);
                assertVerticalOrder(activity, R.id.button_income_next, R.id.button_income_back);
            });
        }

        Intent currencyIntent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                CurrencyActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, 10000d)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, 0.5d);
        try (ActivityScenario<CurrencyActivity> scenario = ActivityScenario.launch(currencyIntent)) {
            scenario.onActivity(activity -> activity.findViewById(R.id.radio_eur).performClick());
            rotate(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    Configuration.ORIENTATION_LANDSCAPE);
            scenario.onActivity(activity -> {
                assertEquals(R.id.radio_eur,
                        ((android.widget.RadioGroup) activity.findViewById(R.id.group_currency))
                                .getCheckedRadioButtonId());
                assertVerticalOrder(activity, R.id.currency_title, R.id.group_currency);
                assertVerticalOrder(activity, R.id.button_currency_calculate, R.id.button_currency_back);
            });
        }

        String requestId = UUID.randomUUID().toString();
        SavingsCalculator.Result result = SavingsCalculator.calculate(10000, 0.5, 40, 42);
        CalculationState state = CalculationState.success(requestId, "USD", 10000, 0.5,
                40, 42, result);
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            CalculationRepository.getInstance().begin(requestId, "USD", 10000, 0.5);
            CalculationRepository.getInstance().succeed(state);
        });
        Intent resultIntent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                ResultActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_REQUEST_ID, requestId)
                .putExtra(SavingsCalculationService.EXTRA_CURRENCY, "USD")
                .putExtra(SavingsCalculationService.EXTRA_INCOME, 10000d)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, 0.5d);
        try (ActivityScenario<ResultActivity> scenario = ActivityScenario.launch(resultIntent)) {
            rotate(scenario, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                    Configuration.ORIENTATION_LANDSCAPE);
            scenario.onActivity(activity -> {
                assertEquals(android.view.View.VISIBLE, activity.findViewById(R.id.result_table).getVisibility());
                assertVerticalOrder(activity, R.id.result_status, R.id.result_table);
                assertVerticalOrder(activity, R.id.result_currency_label, R.id.result_income_label);
                assertHorizontalOrder(activity, R.id.result_income_label, R.id.result_income);
            });
        }
    }

    private static <T extends Activity> void rotate(ActivityScenario<T> scenario, int requested,
                                                     int expectedOrientation) throws InterruptedException {
        scenario.onActivity(activity -> activity.setRequestedOrientation(requested));
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            final boolean[] matched = {false};
            scenario.onActivity(activity -> matched[0] = activity.getResources()
                    .getConfiguration().orientation == expectedOrientation);
            if (matched[0]) return;
            Thread.sleep(50);
        }
        assertTrue("Requested Activity orientation was not applied", false);
    }

    private static void assertVerticalOrder(Activity activity, int firstId, int secondId) {
        Rect first = bounds(activity, firstId);
        Rect second = bounds(activity, secondId);
        assertTrue("Views overlap vertically: " + firstId + " / " + secondId,
                first.bottom <= second.top || second.bottom <= first.top);
    }

    private static void assertHorizontalOrder(Activity activity, int firstId, int secondId) {
        Rect first = bounds(activity, firstId);
        Rect second = bounds(activity, secondId);
        assertTrue("Views overlap horizontally: " + firstId + " / " + secondId,
                first.right <= second.left || second.right <= first.left);
    }

    private static Rect bounds(Activity activity, int viewId) {
        android.view.View view = activity.findViewById(viewId);
        assertTrue("View should exist and be laid out: " + viewId,
                view != null && view.getWidth() > 0 && view.getHeight() > 0);
        int[] position = new int[2];
        view.getLocationOnScreen(position);
        return new Rect(position[0], position[1], position[0] + view.getWidth(),
                position[1] + view.getHeight());
    }
}
