package com.example.savingscalculator;

/** UI events handled by the host Activity. True means the navigation was accepted. */
public interface NavigationCallbacks {
    boolean onStartCalculation();
    boolean onIncomeSubmitted(double monthlyIncome, double share);
    boolean onCurrencySelected(double monthlyIncome, double share, String currency);
    boolean onNavigateBack();
    boolean onReturnHome();
    void onExitRequested();
}
