package com.example.savingscalculator;

public final class CalculationState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    public final String requestId;
    public final Status status;
    public final String currency;
    public final double monthlyIncome;
    public final double share;
    public final double startRate;
    public final double endRate;
    public final SavingsCalculator.Result result;
    public final String errorMessage;

    private CalculationState(String requestId, Status status, String currency,
                             double monthlyIncome, double share, double startRate,
                             double endRate, SavingsCalculator.Result result, String errorMessage) {
        this.requestId = requestId;
        this.status = status;
        this.currency = currency;
        this.monthlyIncome = monthlyIncome;
        this.share = share;
        this.startRate = startRate;
        this.endRate = endRate;
        this.result = result;
        this.errorMessage = errorMessage;
    }

    static CalculationState idle() {
        return new CalculationState(null, Status.IDLE, null, 0, 0, 0, 0, null, null);
    }

    static CalculationState loading(String requestId, String currency, double income, double share) {
        return new CalculationState(requestId, Status.LOADING, currency, income, share,
                0, 0, null, null);
    }

    static CalculationState success(String requestId, String currency, double income, double share,
                                     double start, double end, SavingsCalculator.Result result) {
        return new CalculationState(requestId, Status.SUCCESS, currency, income, share,
                start, end, result, null);
    }

    static CalculationState error(String requestId, String currency, double income,
                                  double share, String message) {
        return new CalculationState(requestId, Status.ERROR, currency, income, share,
                0, 0, null, message);
    }
}
