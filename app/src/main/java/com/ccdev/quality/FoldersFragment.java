package com.ccdev.quality;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
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

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Coleby on 7/2/2016.
 */

// TODO this class will need cleaned up
public class FoldersFragment extends Fragment {

    private TextView mHeader;
    private LinearLayout mBreadCrumbs, mFolderList;
    private Button mNewFolder, mNewScan, mNewPhoto;

    private ArrayList<TextView> mBreadCrumbViews;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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

        mBreadCrumbViews = new ArrayList<>();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    return navigateBack();
                } else {
                    return false;
                }
            }
        });

        populate();
    }

    private boolean navigateBack() {

        // TODO do we go back?

        return false;
    }

    private void populate() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFolderList.removeAllViews();
                mHeader.setText(Networking.getCurrentName().substring(0, Networking.getCurrentName().length()-1));

                for (SmbFile smbFile : Networking.getDirsFiles()) {
                    addFoldersItem(smbFile);
                }
            }
        });
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

    private void addBreadCrumbs() {

        for (int i = mBreadCrumbs.getChildCount(); i < Networking.getBreadCrumbCount(); i++) {

            TextView newCrumb = new TextView(getContext());
            newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
            newCrumb.setText(Networking.getBreadCrumbAt(i).getName());
            newCrumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBreadCrumbViews.remove(v);
                    // Networking.goBackTo(..)
                    // Networking.getBreadCrumbAt(i).getPath()
                    // if ^^ == 0, populate()
                }
            });
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
                fileName.setText(smbFile.getName().substring(0, smbFile.getName().length() - 1));
                fileDetails.setText("");
                loading.setVisibility(View.GONE);
                thumbnail.setBackgroundResource(R.drawable.item_folder);
            } else {
                fileName.setText(smbFile.getName());
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm aa");
                fileDetails.setText(dateFormat.format(smbFile.getDate()));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = BitmapHandler.createOrGetThumbnail(smbFile.getPath());
                        setThumbnail(thumbnail, loading, bitmap);
                    }
                }).start();
            }
        } catch (SmbException e) {
            Log.d("TEST", e.toString());
            // TODO handle this
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO button action
            }
        });

        container.setTag(smbFile.getPath());
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
                                        populate();
                                        break;
                                    default:
                                        // TODO handle errors
                                }
                            } else {
                                final Bitmap bitmap = BitmapHandler.createOrGetThumbnail(smbFile.getPath());
                                if (bitmap != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loading.setVisibility(View.GONE);
                                            thumbnail.setImageBitmap(bitmap);
                                        }
                                    });
                                }
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
