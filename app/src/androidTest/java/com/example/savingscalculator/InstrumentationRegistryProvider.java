package com.example.savingscalculator;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

final class InstrumentationRegistryProvider {
    private InstrumentationRegistryProvider() { }

    static Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
}
