package com.ccdev.quality;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
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
import java.util.Arrays;

/**
 * Created by Coleby on 7/2/2016.
 */

// TODO this class will need cleaned up
public class FoldersFragment extends Fragment {
    private OnFoldersListener mCallback;

    public static String PATH_TO_FILE = "path_to_file";
    public static String IS_ROOT = "is_root";

    public static final int ERROR_GETTING_FILE_TREE = -2;
    public static final int INTERRUPTED_POPULATE = -3;
    public static final int ERROR_PREFS_MISSING = -4;

    private TextView mHeaderText;
    private LinearLayout mBreadCrumbs, mFolderContents;
    private Button mNewFolder, mNewScan, mNewPhoto;

    public interface OnFoldersListener {
        void OnFoldersError(int errorCode, String errorMessage);
        void OnFoldersShowPhoto(String pathToFile);
    }

    // TODO open with loading dialog, timeout?

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
        mBreadCrumbs = (LinearLayout) getView().findViewById(R.id.folders_breadcrumbs);
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

    private void populate(final ArrayList<Networking.NetworkLocation> folderItems) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFolderContents.removeAllViews();
                mHeaderText.setText(Networking.getCurrentName());

                for (Networking.NetworkLocation item : folderItems) {
                    addFoldersItem(item);
                }
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

        String path = Prefs.getAuthString();
        String split[] = currentPath.replace(path, "").split("/");
        String splitRoot[] = Prefs.getRoot().replace(path, "").split("/");
        splitRoot = Arrays.copyOf(splitRoot, splitRoot.length - 1);

        for (int i = 0; i < split.length; i++) {
            path += split[i] + "/";
            if (i >= splitRoot.length) {
                final TextView newCrumb = new TextView(getContext());
                newCrumb.setText(split[i].replace("/", ""));
                newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
                newCrumb.setTag(path);
                newCrumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO navigate
                    }
                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mBreadCrumbs.addView(newCrumb);
                    }
                });
            }
        }
    }

    private void addFoldersItem(final Networking.NetworkLocation item) {
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
            Thread thumbNailThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = BitmapHandler.createOrGetThumbnail(item.getPath());
                    setThumbnail(thumbNailView, loadingView, bitmap);
                }
            });
            thumbNailThread.start();
            mThumbnailThreads.add(thumbNailThread);
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
                    mCallback.OnFoldersNavigateTo(item.getPath());
                } else {
                    mCallback.OnFoldersShowPhoto(item.getPath());
                }
            }
        });

        mFolderContents.addView(itemsLayout);
    }

    private void setThumbnail(final ImageView thumbnailView,
                              final ProgressBar loadingView, final Bitmap bitmap) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingView.setVisibility(View.GONE);
                thumbnailView.setImageBitmap(bitmap);
            }
        });
    }
}
