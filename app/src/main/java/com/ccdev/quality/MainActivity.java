package com.ccdev.quality;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
    implements SettingsFragment.OnSettingsListener,
        FoldersFragment.OnFoldersListener, PhotoViewFragment.OnPhotoViewListener,
        BackHandledFragment.BackHandlerInterface {

    /* global variables */

    private static final String TAG = "Quality.MainActivity";
    private FragmentManager mFragmentManager;
    private BackHandledFragment mFoldersFragment, mPhotoViewFragment;
    private Fragment mLoginFragment, mSettingsFragment;
    private BackHandledFragment selectedFragment;

    /* global threads */

    private Thread mGetFileTreeThread;

    /* layout variables */

    private DrawerLayout mDrawerLayout;

    /* implemented interface - BackHandledFragment */

    @Override
    public void setSelectedFragment(BackHandledFragment backHandledFragment) {
        selectedFragment = backHandledFragment;
    }

    /* implemented interface - FoldersFragment */

    @Override
    public void OnFoldersError(int errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);

        // TODO if loading folders first need to go to settings/login

        switch (errorCode) {
            case FoldersFragment.ERROR_GETTING_FILE_TREE:
                break;
            case FoldersFragment.ERROR_PREFS_MISSING:
                break;
            case FoldersFragment.INTERRUPTED_POPULATE:
                break;
            default:
        }
    }

    @Override
    public void OnFoldersShowPhoto(String pathToFile) {

        Bundle bundle = new Bundle();
        bundle.putString(PhotoViewFragment.IMAGE_PATH, pathToFile);
        mPhotoViewFragment.setArguments(bundle);

        mFragmentManager
                .beginTransaction()
                .add(R.id.content_frame, mPhotoViewFragment)
                .addToBackStack("TEST")
                .commit();
    }

    /* implemented interface - PhotoViewFragment */

    @Override
    public void OnPhotoViewError(int errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);

        // TODO fix this interface
    }

    @Override
    public void OnPhotoViewRemove() {
        selectedFragment = mFoldersFragment;
        mFragmentManager.popBackStack();
    }

    /* implemented interface - SettingsFragment */

    @Override
    public void OnSettingsError(int errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);
        // TODO handle this
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), "Problem logging in", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void OnSettingsConfirm() {
        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK) {
            // TODO tell user why they were kicked back

            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mSettingsFragment)
                    .commit();
        }

        if (Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mLoginFragment)
                    .commit();
        }

        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, mFoldersFragment)
                .commit();
    }

    @Override
    public void OnSettingsCancel() {
        // TODO handle this somehow
    }

    /* class overrides (in order of lifecycle) */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prefs.getPrefs(this);
        System.setProperty("jcifs.smb.client.responseTimeout", "5000");

        mLoginFragment = new LoginFragment();
        mFoldersFragment = new FoldersFragment();
        mSettingsFragment = new SettingsFragment();
        mPhotoViewFragment = new PhotoViewFragment();

        mFragmentManager = getSupportFragmentManager();
        determineLandingPage();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // fragment lifecycle: activity is now running

    @Override
    public void onBackPressed() {
        if (selectedFragment == null || !selectedFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void determineLandingPage() {

        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK) {
            // TODO handle this
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mSettingsFragment)
                    .commit();
            return;
        }

        if (Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            // TODO handle this
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mSettingsFragment)
                    .commit();
            return;
        }

        mGetFileTreeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (Networking.getFileTree()) {
                    mFragmentManager
                            .beginTransaction()
                            .replace(R.id.content_frame, mFoldersFragment)
                            .commit();
                }
            }
        });
        mGetFileTreeThread.start();

        // TODO show loading?
    }
}
