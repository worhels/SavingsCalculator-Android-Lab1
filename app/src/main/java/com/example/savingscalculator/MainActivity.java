package com.example.savingscalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements NavigationCallbacks {
    private boolean navigationPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            view.setPadding(systemBars.left, systemBars.top,
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> navigationPending = false);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    private boolean canNavigate() {
        return !navigationPending && !getSupportFragmentManager().isStateSaved()
                && !isFinishing() && !isDestroyed();
    }

    private boolean showNext(Class<? extends Fragment> expected, Fragment next) {
        if (!canNavigate() || !expected.isInstance(
                getSupportFragmentManager().findFragmentById(R.id.fragment_container))) return false;
        navigationPending = true;
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, next)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public boolean onStartCalculation() {
        return showNext(HomeFragment.class, IncomeFragment.newInstance());
    }

    @Override
    public boolean onIncomeSubmitted(double monthlyIncome, double share) {
        return showNext(IncomeFragment.class, CurrencyFragment.newInstance(monthlyIncome, share));
    }

    @Override
    public boolean onCurrencySelected(double monthlyIncome, double share, String currency) {
        return showNext(CurrencyFragment.class, ResultFragment.newInstance(monthlyIncome, share, currency));
    }

    @Override
    public boolean onNavigateBack() {
        if (!canNavigate() || getSupportFragmentManager().getBackStackEntryCount() == 0) return false;
        navigationPending = true;
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    public boolean onReturnHome() {
        if (!canNavigate() || !(getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof ResultFragment)) return false;
        navigationPending = true;
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        return true;
    }

    @Override
    public void onExitRequested() {
        if (canNavigate() && getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof HomeFragment) finish();
    }
}
