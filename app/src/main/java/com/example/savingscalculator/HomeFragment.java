package com.example.savingscalculator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HomeFragment extends NavigationFragment {

    @NonNull
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_start).setOnClickListener(v -> {
            if (v.isEnabled() && navigation().onStartCalculation()) v.setEnabled(false);
        });

        view.findViewById(R.id.button_exit).setOnClickListener(v ->
                navigation().onExitRequested());
    }
}
