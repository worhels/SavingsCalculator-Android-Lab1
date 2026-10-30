package com.example.savingscalculator;

import android.content.Intent;
import android.app.Activity;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResultFragmentTest {
    @Test public void serviceLoadsRatesAndObserverReceivesSuccessAcrossRecreation() throws Exception {
        CalculationRepository repository = CalculationRepository.getInstance();
        CountDownLatch firstCompleted = new CountDownLatch(1);
        CountDownLatch secondCompleted = new CountDownLatch(1);
        AtomicReference<CalculationState> observed = new AtomicReference<>();
        CalculationObserver observer = state -> {
            if (state.status == CalculationState.Status.SUCCESS
                    || state.status == CalculationState.Status.ERROR) {
                if ("EUR".equals(state.currency)) firstCompleted.countDown();
                if ("USD".equals(state.currency)) {
                    observed.set(state);
                    secondCompleted.countDown();
                }
            }
        };
        Intent currencyIntent = new Intent(InstrumentationRegistryProvider.context(), CurrencyActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, 10000d)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, 0.5);
        try (ActivityScenario<CurrencyActivity> scenario = ActivityScenario.launch(currencyIntent)) {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> repository.begin(
                    java.util.UUID.randomUUID().toString(), "EUR", 10000d, 0.5d));
            repository.observe(observer);
            scenario.onActivity(activity -> {
                ((RadioGroup) activity.findViewById(R.id.group_currency)).check(R.id.radio_eur);
                activity.findViewById(R.id.button_currency_calculate).performClick();
            });
            assertTrue("First calculation did not finish", firstCompleted.await(10, TimeUnit.SECONDS));
            long resumeDeadline = System.currentTimeMillis() + 5_000;
            boolean resultResumed = false;
            while (!resultResumed && System.currentTimeMillis() < resumeDeadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync();
                final boolean[] found = {false};
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    Collection<Activity> resumed = ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(Stage.RESUMED);
                    for (Activity activity : resumed) {
                        if (activity instanceof ResultActivity) found[0] = true;
                    }
                });
                resultResumed = found[0];
                if (!resultResumed) Thread.sleep(50);
            }
            assertTrue("ResultActivity should resume before Back", resultResumed);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Collection<Activity> resumed = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED);
                for (Activity activity : resumed) {
                    if (activity instanceof ResultActivity) {
                        ((ResultActivity) activity).getOnBackPressedDispatcher().onBackPressed();
                        break;
                    }
                }
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            long returnDeadline = System.currentTimeMillis() + 5_000;
            boolean calculateEnabled = false;
            while (!calculateEnabled && System.currentTimeMillis() < returnDeadline) {
                final boolean[] enabled = {false};
                scenario.onActivity(activity -> enabled[0] =
                        activity.findViewById(R.id.button_currency_calculate).isEnabled());
                calculateEnabled = enabled[0];
                if (!calculateEnabled) Thread.sleep(50);
            }
            assertTrue("CurrencyActivity should resume after Back", calculateEnabled);
            scenario.onActivity(activity -> {
                ((RadioGroup) activity.findViewById(R.id.group_currency)).check(R.id.radio_usd);
                activity.findViewById(R.id.button_currency_calculate).performClick();
            });
            assertTrue("Second calculation did not finish", secondCompleted.await(10, TimeUnit.SECONDS));
            repository.removeObserver(observer);
        }
        CalculationState state = observed.get();
        assertNotNull(state);
        assertEquals(CalculationState.Status.SUCCESS, state.status);
        assertEquals("USD", state.currency);
        assertEquals(40, state.startRate, 0);
        assertEquals(42, state.endRate, 0);
        assertTrue(state.result.financialResult > 0);

        Intent resultIntent = new Intent(InstrumentationRegistryProvider.context(), ResultActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_REQUEST_ID, state.requestId)
                .putExtra(SavingsCalculationService.EXTRA_CURRENCY, state.currency)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, state.monthlyIncome)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, state.share);
        try (ActivityScenario<ResultActivity> scenario = ActivityScenario.launch(resultIntent)) {
            scenario.onActivity(activity -> assertEquals(activity.getString(R.string.sc_result_profit),
                    ((TextView) activity.findViewById(R.id.result_status)).getText().toString()));
            scenario.recreate();
            scenario.onActivity(activity -> {
                assertEquals("USD — долар США", ((TextView) activity.findViewById(R.id.result_currency)).getText().toString());
                assertEquals(CalculationState.Status.SUCCESS, repository.getCurrent().status);
            });
        }
    }
}
