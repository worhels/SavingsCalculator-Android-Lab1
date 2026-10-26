package com.example.savingscalculator;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CalculationRepository {
    private static final CalculationRepository INSTANCE = new CalculationRepository();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<WeakReference<CalculationObserver>> observers = new ArrayList<>();
    private CalculationState current = CalculationState.idle();

    private CalculationRepository() { }

    public static CalculationRepository getInstance() { return INSTANCE; }

    public void observe(@NonNull CalculationObserver observer) {
        runOnMain(() -> {
            removeObserverNow(observer);
            observers.add(new WeakReference<>(observer));
            observer.onCalculationChanged(current);
        });
    }

    public void removeObserver(@NonNull CalculationObserver observer) {
        runOnMain(() -> removeObserverNow(observer));
    }

    public void begin(String requestId, String currency, double income, double share) {
        runOnMain(() -> {
            current = CalculationState.loading(requestId, currency, income, share);
            notifyObservers(current);
        });
    }

    void succeed(CalculationState state) {
        complete(state);
    }

    void fail(String requestId, String currency, double income, double share, String message) {
        complete(CalculationState.error(requestId, currency, income, share, message));
    }

    public CalculationState getCurrent() { return current; }

    private void complete(CalculationState state) {
        runOnMain(() -> {
            if (current.status != CalculationState.Status.LOADING
                    || current.requestId == null
                    || !current.requestId.equals(state.requestId)) return;
            current = state;
            notifyObservers(state);
        });
    }

    private void notifyObservers(CalculationState state) {
        Iterator<WeakReference<CalculationObserver>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            CalculationObserver observer = iterator.next().get();
            if (observer == null) iterator.remove();
            else observer.onCalculationChanged(state);
        }
    }

    private void removeObserverNow(CalculationObserver observer) {
        Iterator<WeakReference<CalculationObserver>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            CalculationObserver value = iterator.next().get();
            if (value == null || value == observer) iterator.remove();
        }
    }

    private void runOnMain(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) action.run();
        else mainHandler.post(action);
    }
}
