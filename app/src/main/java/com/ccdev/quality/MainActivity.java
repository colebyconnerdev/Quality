package com.ccdev.quality;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

public class MainActivity extends AppCompatActivity
    implements LoginFragment.OnLoginListener, SettingsFragment.OnSettingsListener,
        FoldersFragment.OnFoldersListener, PhotoViewFragment.OnPhotoViewListener {

    private DrawerLayout mDrawerLayout;

    private FragmentManager mFragmentManager;
    private Fragment mLoginFragment, mFoldersFragment, mSettingsFragment, mPhotoViewFragment;

    @Override
    public void OnShowPhoto(String pathToFile) {

        Bundle bundle = new Bundle();
        bundle.putString(PhotoViewFragment.IMAGE_PATH, pathToFile);
        mPhotoViewFragment.setArguments(bundle);

        mFragmentManager
                .beginTransaction()
                .add(R.id.content_frame, mPhotoViewFragment)
                .addToBackStack(null)
                .commit();
    }

    // TODO figure out the back button

    @Override
    public void OnRemovePhotoView() {
        mFragmentManager.popBackStack();
    }

    @Override
    public void OnNoPhotoPath() {
        mFragmentManager.popBackStack();
    }

    @Override
    public void OnLoginResult(int status) {

        if (status != Networking.RESULT_OK) {
            // TODO handle this
        }

        Prefs.commitUsername();
        Prefs.commitPassword();
        Prefs.commitRememberMe();

        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, mFoldersFragment)
                .commit();
    }

    @Override
    public void OnLoginSettings() {
        // TODO clean up?
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, mSettingsFragment)
                .commit();
    }

    @Override
    public void OnSettingsConfirm() {
        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK) {
            // TODO handle this
        }

        if (Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mLoginFragment)
                    .commit();
        }

        // TODO loading dialog?

        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (Networking.getRoot()) {
                    case Networking.RESULT_OK:
                        mFragmentManager
                                .beginTransaction()
                                .replace(R.id.content_frame, mFoldersFragment)
                                .commit();
                        break;
                    default:
                        // TODO errors
                }
            }
        }).start();
    }

    @Override
    public void OnSettingsCancel() {
        // TODO handle this somehow...
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Prefs.getPrefs(this);

        mLoginFragment = new LoginFragment();
        mFoldersFragment = new FoldersFragment();
        mSettingsFragment = new SettingsFragment();
        mPhotoViewFragment = new PhotoViewFragment();

        mFragmentManager = getSupportFragmentManager();
        determineLandingPage();
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

        if (Prefs.checkUserSettings() != Prefs.checkServerSettings()) {
            // TODO handle this
            mFragmentManager
                    .beginTransaction()
                    .replace(R.id.content_frame, mSettingsFragment)
                    .commit();
            return;
        }

        // TODO loading dialog?

        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (Networking.getRoot()) {
                    case Networking.RESULT_OK:
                        mFragmentManager
                                .beginTransaction()
                                .replace(R.id.content_frame, mFoldersFragment)
                                .commit();
                        break;
                    default:
                        // TODO errors
                }
            }
        }).start();
    }
}
