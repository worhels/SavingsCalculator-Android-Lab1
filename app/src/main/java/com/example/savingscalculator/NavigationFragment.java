package com.example.savingscalculator;

import android.content.Context;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/** Binds callbacks to the current host, including after Activity recreation. */
public abstract class NavigationFragment extends Fragment {
    private NavigationCallbacks callbacks;

    protected NavigationFragment() { }

    protected NavigationFragment(@LayoutRes int layout) {
        super(layout);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof NavigationCallbacks)) {
            throw new IllegalStateException("Host must implement NavigationCallbacks");
        }
        callbacks = (NavigationCallbacks) context;
    }

    protected final NavigationCallbacks navigation() {
        if (callbacks == null) throw new IllegalStateException("Fragment is not attached");
        return callbacks;
    }

    @Override
    public void onDetach() {
        callbacks = null;
        super.onDetach();
    }
}
