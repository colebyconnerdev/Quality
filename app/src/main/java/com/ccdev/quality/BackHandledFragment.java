package com.ccdev.quality;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Coleby on 7/9/2016.
 */

public abstract class BackHandledFragment extends Fragment {

    protected BackHandlerInterface backHandledInterface;
    public abstract boolean onBackPressed();

    public interface BackHandlerInterface {
        public void setSelectedFragment(BackHandledFragment backHandledFragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!(getActivity() instanceof BackHandlerInterface)) {
            throw new ClassCastException("Hosting activity must implement BackHandledInterface");
        } else {
            backHandledInterface = (BackHandlerInterface) getActivity();
        }
    }
}
