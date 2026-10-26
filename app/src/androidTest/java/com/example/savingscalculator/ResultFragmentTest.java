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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ResultFragmentTest {
    @Test public void serviceLoadsRatesAndObserverReceivesSuccessAcrossRecreation() throws Exception {
        CalculationRepository repository = CalculationRepository.getInstance();
        CountDownLatch firstCompleted = new CountDownLatch(1);
        CountDownLatch secondCompleted = new CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger terminalCount = new java.util.concurrent.atomic.AtomicInteger();
        AtomicReference<CalculationState> observed = new AtomicReference<>();
        CalculationObserver observer = state -> {
            if (state.status == CalculationState.Status.SUCCESS
                    || state.status == CalculationState.Status.ERROR) {
                observed.set(state);
                if (terminalCount.incrementAndGet() == 1) firstCompleted.countDown();
                else secondCompleted.countDown();
            }
        };
        Intent currencyIntent = new Intent(InstrumentationRegistryProvider.context(), CurrencyActivity.class)
                .putExtra(SavingsCalculationService.EXTRA_INCOME, 10000d)
                .putExtra(SavingsCalculationService.EXTRA_SHARE, 0.5);
        try (ActivityScenario<CurrencyActivity> scenario = ActivityScenario.launch(currencyIntent)) {
            repository.observe(observer);
            scenario.onActivity(activity -> {
                ((RadioGroup) activity.findViewById(R.id.group_currency)).check(R.id.radio_eur);
                activity.findViewById(R.id.button_currency_calculate).performClick();
            });
            assertTrue("First calculation did not finish", firstCompleted.await(10, TimeUnit.SECONDS));
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                Collection<Activity> resumed = ActivityLifecycleMonitorRegistry.getInstance()
                        .getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> iterator = resumed.iterator();
                if (iterator.hasNext()) {
                    ((androidx.activity.ComponentActivity) iterator.next())
                            .getOnBackPressedDispatcher().onBackPressed();
                }
            });
            scenario.onActivity(activity -> {
                assertTrue(activity.findViewById(R.id.button_currency_calculate).isEnabled());
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
