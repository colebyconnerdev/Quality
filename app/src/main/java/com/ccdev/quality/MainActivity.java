package com.ccdev.quality;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

public class MainActivity extends AppCompatActivity
    implements SettingsFragment.OnSettingsListener,
        FoldersFragment.OnFoldersListener, PhotoViewFragment.OnPhotoViewListener {
    private static final String TAG = "Quality.MainActivity";

    Thread mGetFileTreeThread;

    private DrawerLayout mDrawerLayout;

    private FragmentManager mFragmentManager;
    private Fragment mLoginFragment, mFoldersFragment, mSettingsFragment, mPhotoViewFragment;
    private static final String FRAGMENT_FOLDERS = "fragment_folders";

    @Override
    public void OnFoldersShowPhoto(String pathToFile) {

        Bundle bundle = new Bundle();
        bundle.putString(PhotoViewFragment.IMAGE_PATH, pathToFile);
        mPhotoViewFragment.setArguments(bundle);

        mFragmentManager
                .beginTransaction()
                .add(R.id.content_frame, mPhotoViewFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void OnFoldersError(int errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);

        // TODO if loading folders first need to go to settings/login

        switch (errorCode) {
            case FoldersFragment.ERROR_GETTING_FILE_TREE:
                mFragmentManager.popBackStack();
                break;
            case FoldersFragment.ERROR_PREFS_MISSING:
                mFragmentManager.popBackStackImmediate(
                        FRAGMENT_FOLDERS, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                mFragmentManager
                        .beginTransaction()
                        .replace(R.id.content_frame, mSettingsFragment)
                        .commit();
                break;
            case FoldersFragment.INTERRUPTED_POPULATE:
                mFragmentManager.popBackStack();
                break;
            default:
        }
    }

    @Override
    public void OnPhotoViewError(int errorCode, String errorMessage) {
        Log.e(TAG, errorMessage);

        // TODO fix this interface

        mFragmentManager.popBackStack();
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

    @Override
    public void onBackPressed() {
        if (mFoldersFragment.isVisible()) {
            Networking.goBack();
            mFragmentManager.popBackStack();
        } else if (mPhotoViewFragment.isVisible()) {

        } else {
            //super.onBackPressed();
        }
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
