package com.ccdev.quality;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ccdev.quality.Utils.BitmapHandler;
import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Coleby on 7/2/2016.
 */

// TODO this class will need cleaned up
public class FoldersFragment extends BackHandledFragment {

    private static final String TAG = "Quality.FoldersFragment";

    private OnFoldersListener mCallback;

    public static String PATH_TO_FILE = "path_to_file";
    public static String IS_ROOT = "is_root";

    public static final int ERROR_GETTING_FILE_TREE = -2;
    public static final int INTERRUPTED_POPULATE = -3;
    public static final int ERROR_PREFS_MISSING = -4;

    private TextView mHeaderText;
    private LinearLayout mBreadCrumbViews, mFolderContents, mLoadingBack;
    private Button mNewFolder, mNewScan, mNewPhoto;

    private Stack<String> mPaths = new Stack<>();

    public interface OnFoldersListener {
        void OnFoldersError(int errorCode, String errorMessage);
        void OnFoldersShowPhoto(String pathToFile);
    }

    @Override
    public boolean onBackPressed() {
        // TODO grab returned thread
        goBackThreaded();
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnFoldersListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFoldersListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folders, container, false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // TODO validate path

        mHeaderText = (TextView) getView().findViewById(R.id.folders_header);
        mFolderContents = (LinearLayout) getView().findViewById(R.id.folders_list);
        mBreadCrumbViews = (LinearLayout) getView().findViewById(R.id.folders_breadcrumbs);
        mLoadingBack = (LinearLayout) getView().findViewById(R.id.folders_loadingBack);
        mNewFolder = (Button) getView().findViewById(R.id.folders_newFolder);
        mNewScan = (Button) getView().findViewById(R.id.folders_newScan);
        mNewPhoto = (Button) getView().findViewById(R.id.folders_newPhoto);

        ArrayList<Networking.NetworkLocation> folderItems = Networking.getDirsFiles();

        if (folderItems != null && Networking.getStatus() == Networking.SUCCESS) {
            populate(folderItems);
            addBreadCrumbs(Networking.getCurrentPath());
        } else {
            mCallback.OnFoldersError(ERROR_GETTING_FILE_TREE,
                    "FoldersFragment.onActivityCreated(): " + Networking.getStatusMessage());
        }
    }

    private Thread goBackThreaded() {
        Thread goBackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                goBack();
            }
        });
        goBackThread.start();

        return goBackThread;
    }

    private void goBack() {
        if (mPaths != null && mPaths.size() > 1) {
            // TODO grab returned thread
            getFileTreeThreaded(mPaths.get(mPaths.size()-2));
        }
    }

    private Thread getFileTreeThreaded(final String pathToFile) {
        Thread getFileTreeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getFileTree(pathToFile);
            }
        });
        getFileTreeThread.start();

        return getFileTreeThread;
    }

    private void getFileTree(String pathToFile) {
        ArrayList<Networking.NetworkLocation> folderItems;
        if (Networking.getFileTree(pathToFile)) {
            folderItems= Networking.getDirsFiles();
        } else {
            // TODO make a static error
            mCallback.OnFoldersError(0, "FoldersFragment.getFileTree(): " + Networking.getStatusMessage());
            return;
        }

        populateOnUiThread(folderItems);
        addBreadCrumbsOnUiThread(pathToFile);
    }

    private void populateOnUiThread(final ArrayList<Networking.NetworkLocation> folderItems) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                populate(folderItems);
            }
        });
    }

    private void populate(final ArrayList<Networking.NetworkLocation> folderItems) {
        mFolderContents.removeAllViews();
        mHeaderText.setText(Networking.getCurrentName());

        for (final Networking.NetworkLocation item : folderItems) {

            ConstraintLayout itemsLayout = (ConstraintLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_folders, null);

            final ImageView thumbNailView = (ImageView) itemsLayout.findViewById(R.id.item_folders_thumbnail);
            final ProgressBar loadingView = (ProgressBar) itemsLayout.findViewById(R.id.item_folders_loading);
            TextView nameView = (TextView) itemsLayout.findViewById(R.id.item_folders_fileName);
            TextView detailsView = (TextView) itemsLayout.findViewById(R.id.item_folders_fileDetials);
            Button editButton = (Button) itemsLayout.findViewById(R.id.item_folders_button);

            nameView.setText(item.getName());
            detailsView.setText(item.getDetails());

            if (item.isDir()) {
                loadingView.setVisibility(View.GONE);
                thumbNailView.setBackgroundResource(R.drawable.item_folder);
            } else {
                // TODO grab returned thread
                getOrCreateThumbnailThreaded(item.getPath(), thumbNailView, loadingView);
            }

            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO button action
                }
            });

            itemsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isDir()) {
                        // TODO grab returned thread
                        getFileTreeThreaded(item.getPath());
                    } else {
                        mCallback.OnFoldersShowPhoto(item.getPath());
                    }
                }
            });

            mFolderContents.addView(itemsLayout);
        }
    }

    private Thread getOrCreateThumbnailThreaded(final String path,
            final ImageView thumbnailView, final ProgressBar loadingView) {

        Thread getOrCreateThumbnailThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapHandler.createOrGetThumbnail(path);
                setThumbnailOnUiThread(thumbnailView, loadingView, bitmap);
            }
        });
        getOrCreateThumbnailThread.start();

        return getOrCreateThumbnailThread;
    }

    private void setThumbnailOnUiThread(final ImageView thumbnailView,
                              final ProgressBar loadingView, final Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingView.setVisibility(View.GONE);
                thumbnailView.setImageBitmap(bitmap);
            }
        });
    }

    private void addBreadCrumbsOnUiThread(final String currentPath) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addBreadCrumbs(currentPath);
            }
        });
    }

    private void addBreadCrumbs(String currentPath) {
        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK || Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            mCallback.OnFoldersError(ERROR_PREFS_MISSING,
                    "FoldersFragment.addBreadCrumbs(): server and/or user settings not valid");
            return;
        }

        // TODO validation?

        if (mPaths.isEmpty()) {
            String[] rootSplit = Prefs.getRoot().split("/");
            mPaths.add(currentPath);
            mBreadCrumbViews.addView(getBreadCrumbView(rootSplit[rootSplit.length - 1], currentPath));
            return;
        }

        String lastPath = mPaths.peek();

        String[] currentSplits = currentPath.replace(Prefs.getAuthString(), "").split("/");
        String name = currentSplits[currentSplits.length - 1];

        if (currentPath.length() == lastPath.length()) {
            return;
        } else if (currentPath.length() > lastPath.length()) {
            mPaths.add(currentPath);
            mBreadCrumbViews.addView(getBreadCrumbView(name, currentPath));
            return;
        }

        int mark = 0;
        for (int i = 0; i < mBreadCrumbViews.getChildCount(); i++) {
            if (((TextView) mBreadCrumbViews.getChildAt(i)).getText().equals(name)) {
                mark = i;
                break;
            }
        }

        for (int i = mBreadCrumbViews.getChildCount() - 1; i > mark; i--) {
            mPaths.pop();
            mBreadCrumbViews.removeViewAt(i);
        }
    }

    private TextView getBreadCrumbView(String name, String path) {
        TextView newCrumb = new TextView(getContext());
        newCrumb.setText(name);
        newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
        newCrumb.setTag(path);
        newCrumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO grab returned thread
                getFileTreeThreaded((String) v.getTag());
            }
        });

        return newCrumb;
    }
}
