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

import com.ccdev.quality.Utils.Prefs;

/**
 * Created by Coleby on 7/1/2016.
 */

public class SettingsFragment extends Fragment {
    private OnSettingsListener mCallback;

    public interface OnSettingsListener {
        void OnSettingsConfirm();
        void OnSettingsCancel();
    }

    private EditText mServerInput, mRootInput, mDomainInput, mUsernameInput, mPasswordInput;
    private CheckBox mRememberMeInput;
    private Button mCancel, mConfirm;

    private String mServer, mRoot, mDomain, mUsername, mPassword;
    private boolean mRememberMe;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnSettingsListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSettingsChangeListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initialize();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    // TODO call when onStart()?
    private void initialize() {

        // TODO admin password
        mServerInput = (EditText) getView().findViewById(R.id.settings_server);
        mRootInput = (EditText) getView().findViewById(R.id.settings_root);
        mDomainInput = (EditText) getView().findViewById(R.id.settings_domain);
        mUsernameInput = (EditText) getView().findViewById(R.id.settings_username);
        mPasswordInput = (EditText) getView().findViewById(R.id.settings_password);
        mRememberMeInput = (CheckBox) getView().findViewById(R.id.settings_rememberMe);
        mCancel = (Button) getView().findViewById(R.id.settings_cancel);
        mConfirm = (Button) getView().findViewById(R.id.settings_confirm);

        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        mServer = Prefs.getServer();
        mRoot = Prefs.getRoot();
        mDomain = Prefs.getDomain();
        mUsername = Prefs.getUsername();
        mPassword = Prefs.getPassword();
        mRememberMe = Prefs.getRememberMe();

        if (mServer.length() != 0) {
            mServerInput.setText(mServer);
        }

        if (mRoot.length() != 0) {
            mRootInput.setText(mRoot);
        }

        if (mDomain.length() != 0) {
            mDomainInput.setText(mDomain);
        }

        if (mUsername.length() != 0) {
            mUsernameInput.setText(mUsername);
        }

        if (mPassword.length() != 0) {
            mPasswordInput.setText(mPassword);
        }

        mRememberMeInput.setChecked(mRememberMe);
    }

    private void confirm() {

        String serverInput;
        if ((serverInput = mServerInput.getText().toString()).isEmpty()) {
            // TODO error?
        } else {
            Prefs.setServer(serverInput);
        }

        String rootInput;
        if ((rootInput = mRootInput.getText().toString()).isEmpty()) {
            // TODO error?
        } else {
            Prefs.setRoot(rootInput);
        }

        String domainInput;
        if ((domainInput = mDomainInput.getText().toString()).isEmpty()) {
            // TODO error?
        } else {
            Prefs.setDomain(domainInput);
        }

        String usernameInput;
        if ((usernameInput = mUsernameInput.getText().toString()).isEmpty()) {
            // TODO error?
        } else {
            Prefs.setUsername(usernameInput);
        }

        String passwordInput;
        if ((passwordInput = mPasswordInput.getText().toString()).isEmpty()) {
            // TODO error?
        } else {
            Prefs.setPassword(passwordInput);
        }

        Prefs.setRememberMe(mRememberMeInput.isChecked());

        // TODO validation?

        Prefs.commitAll();

        mCallback.OnSettingsConfirm();
    }

    private void cancel() {
        mCallback.OnSettingsCancel();
    }
}
