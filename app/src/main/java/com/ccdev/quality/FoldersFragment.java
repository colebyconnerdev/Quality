package com.ccdev.quality;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ccdev.quality.Utils.BitmapDownloader;
import com.ccdev.quality.Utils.Networking;
import com.ccdev.quality.Utils.Prefs;

import org.w3c.dom.Text;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Coleby on 7/2/2016.
 */

// TODO this class will need cleaned up
public class FoldersFragment extends Fragment {

    private Handler mHandler = new Handler();
    private OnFoldersListener mCallback;

    private TextView mHeader;
    private LinearLayout mBreadCrumbs, mFolderList;
    private Button mNewFolder, mNewScan, mNewPhoto;

    private ImageView mTest;

    private ArrayList<TextView> mBreadCrumbViews;

    public interface OnFoldersListener {
        void todo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnFoldersListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFoldersListener");
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

        mHeader = (TextView) getView().findViewById(R.id.folders_header);
        mFolderList = (LinearLayout) getView().findViewById(R.id.folders_list);
        mBreadCrumbs = (LinearLayout) getView().findViewById(R.id.folders_breadcrumbs);
        mNewFolder = (Button) getView().findViewById(R.id.folders_newFolder);
        mNewScan = (Button) getView().findViewById(R.id.folders_newScan);
        mNewPhoto = (Button) getView().findViewById(R.id.folders_newPhoto);

        mTest = (ImageView) getView().findViewById(R.id.folders_imgTest);

        mBreadCrumbViews = new ArrayList<>();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    // TODO cancel downloads - Handler.getLooper().quit() ???
                    switch(Networking.goBack()) {
                        case Networking.RESULT_OK:
                            mHandler.post(populate);
                            return true;
                        default:
                            // TODO handle errors
                            return false;
                    }
                } else {
                    return false;
                }
            }
        });

        mHandler.post(populate);
    }

    private Runnable populate = new Runnable() {
        @Override
        public void run() {

            if (Networking.getDirs().size() == 0) {
                // TODO handle this
            }

            if (Networking.getFiles().size() == 0) {
                // TODO handle this
            }

            mFolderList.removeAllViews();

            mHeader.setText(Networking.getCurrentName());

            for (SmbFile smbFile : Networking.getDirsFiles()) {
                addFoldersItem(smbFile);
            }
        }
    };

    private void addBreadCrumbs() {

        // TODO there has got to be a better way to do this...

        final String rootPath = String.format("smb://%s;%s:%s@%s/%s",
                Prefs.getDomain(), Prefs.getUsername(), Prefs.getPassword(), Prefs.getServer(), Prefs.getRoot());
        String[] splits = Prefs.getRoot().split("/");
        String rootName = splits[splits.length-1];

        TextView newCrumb = new TextView(getContext());
        newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
        newCrumb.setText(rootName);
        newCrumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(Networking.getFileTree(rootPath)) {
                    case Networking.RESULT_OK:
                        int pos = mBreadCrumbViews.indexOf(v);
                        while (mBreadCrumbViews.get(pos) != null) {
                            mBreadCrumbViews.remove(pos);
                        }
                        mHandler.post(populate);
                        break;
                    default:
                        // TODO handle errors
                }
            }
        });

        mBreadCrumbs.addView(newCrumb);
        mBreadCrumbViews.add(newCrumb);

        String path = rootPath;
        splits = Networking.getCurrentPath().split(rootPath + "/");
        if (splits.length > 0) {
            for (int i = 0; i < splits.length; i++) {
                path += "/" + splits[i];
                final String newPath = path;
                newCrumb = new TextView(getContext());
                newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
                newCrumb.setText(splits[i]);
                newCrumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch(Networking.getFileTree(newPath)) {
                            case Networking.RESULT_OK:
                                int pos = mBreadCrumbViews.indexOf(v);
                                while (mBreadCrumbViews.get(pos) != null) {
                                    mBreadCrumbViews.remove(pos);
                                }
                                mHandler.post(populate);
                                break;
                            default:
                                // TODO handle errors
                        }
                    }
                });

                mBreadCrumbs.addView(newCrumb);
                mBreadCrumbViews.add(newCrumb);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    private void addFoldersItem(final SmbFile smbFile) {
        final ConstraintLayout itemsLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_folders, null);

        final ImageView thumbnail = (ImageView) itemsLayout.findViewById(R.id.item_folders_thumbnail);
        final ProgressBar loading = (ProgressBar) itemsLayout.findViewById(R.id.item_folders_loading);
        TextView fileName = (TextView) itemsLayout.findViewById(R.id.item_folders_fileName);
        final TextView fileDetails = (TextView) itemsLayout.findViewById(R.id.item_folders_fileDetials);
        Button button = (Button) itemsLayout.findViewById(R.id.item_folders_button);
        ConstraintLayout container = (ConstraintLayout) itemsLayout.findViewById(R.id.item_folders_layout);

        try {
            if (smbFile.isDirectory()) {
                loading.setVisibility(View.GONE);
                thumbnail.setBackgroundResource(R.drawable.item_folder);
            } else {
                // TODO this breaks the thumbnails, way too slow
//                ViewTreeObserver vto = itemsLayout.getViewTreeObserver();
//                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        if (itemsLayout.getVisibility() == View.VISIBLE) {
//                            removeOnGlobalLayoutListener(itemsLayout, this);
//                            BitmapDownloader.loadBitmap(smbFile.getPath(), loading, thumbnail, fileDetails, thumbnail.getWidth(), thumbnail.getHeight());
//                        }
//                    }
//                });
            }
        } catch (SmbException e) {
            Log.d("TEST", e.toString());
            // TODO handle this
        }

        fileName.setText(smbFile.getName());//.substring(0, 5));
        fileDetails.setText("details");          // TODO get details

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO button action
            }
        });

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (smbFile.isDirectory()) {
                                switch (Networking.getFileTree(smbFile.getPath())) {
                                    case Networking.RESULT_OK:
                                        mHandler.post(populate);
                                        break;
                                    default:
                                        // TODO handle errors
                                }
                            } else {
                                String newPath = smbFile.getPath().replace(smbFile.getName(), "thumbs/" + smbFile.getName());
                                BitmapDownloader.loadBitmap(newPath, loading, thumbnail, fileDetails, thumbnail.getWidth(), thumbnail.getHeight());
                            }
                        } catch (SmbException e) {
                            Log.d("TEST", e.toString());
                        }
                    }
                }).start();
            }
        });

        mFolderList.addView(itemsLayout);
    }

}
