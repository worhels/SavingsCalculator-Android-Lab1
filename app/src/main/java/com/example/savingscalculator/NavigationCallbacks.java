package com.example.savingscalculator;

public interface NavigationCallbacks {
    boolean onStartCalculation();
    boolean onIncomeSubmitted(double monthlyIncome, double share);
    boolean onCurrencySelected(double monthlyIncome, double share, String currency);
    boolean onNavigateBack();
    boolean onReturnHome();
    void onExitRequested();
}
