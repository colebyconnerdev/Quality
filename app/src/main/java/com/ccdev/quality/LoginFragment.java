package com.ccdev.quality;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

/**
 * Created by Coleby on 7/2/2016.
 */

public class LoginFragment extends Fragment{
    private OnLoginListener mCallback;

    public interface OnLoginListener {
    }

    private EditText mUsernameInput, mPasswordInput;
    private CheckBox mRememberMeInput;
    private Button mLogin, mSettings;

    // TODO this class is not complete

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnLoginListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnLoginListener");
        }
    }

    private void initialize() {

        mUsernameInput = (EditText) getView().findViewById(R.id.login_username);
        mPasswordInput = (EditText) getView().findViewById(R.id.login_password);
        mRememberMeInput = (CheckBox) getView().findViewById(R.id.login_rememberMe);
        mLogin = (Button) getView().findViewById(R.id.login_confirm);
        mSettings = (Button) getView().findViewById(R.id.login_serverSettings);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO validation
                Prefs.setUsername(mUsernameInput.getText().toString());
                Prefs.setPassword(mPasswordInput.getText().toString());
                Prefs.setRememberMe(mRememberMeInput.isChecked());

                // TODO loading dialog?
            }
        });

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO handle logon
            }
        });
    }
}
