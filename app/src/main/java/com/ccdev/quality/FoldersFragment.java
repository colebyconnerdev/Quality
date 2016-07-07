package com.ccdev.quality;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Coleby on 7/2/2016.
 */

// TODO this class will need cleaned up
public class FoldersFragment extends Fragment {
    private OnFoldersListener mCallback;

    private TextView mHeader;
    private LinearLayout mBreadCrumbViews, mFolderList;
    private Button mNewFolder, mNewScan, mNewPhoto;

    public interface OnFoldersListener {
        void OnShowPhoto(String pathToFile);
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

        mHeader = (TextView) getView().findViewById(R.id.folders_header);
        mFolderList = (LinearLayout) getView().findViewById(R.id.folders_list);
        mBreadCrumbViews = (LinearLayout) getView().findViewById(R.id.folders_breadcrumbs);
        mNewFolder = (Button) getView().findViewById(R.id.folders_newFolder);
        mNewScan = (Button) getView().findViewById(R.id.folders_newScan);
        mNewPhoto = (Button) getView().findViewById(R.id.folders_newPhoto);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if (Networking.getBreadCrumbCount() > 1) {
                        mBreadCrumbViews.removeViewAt(mBreadCrumbViews.getChildCount() - 1);
                        Networking.goBack();
                        populate();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        addBreadCrumb();
        populate();
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

    private void addBreadCrumb() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                TextView newCrumb = new TextView(getContext());
                newCrumb.setBackgroundResource(R.drawable.breadcrumbs);
                newCrumb.setTag(Networking.getLastBreadCrumb());
                newCrumb.setText(Networking.getLastBreadCrumb().getName());
                newCrumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Networking.BreadCrumb breadCrumb = (Networking.BreadCrumb) v.getTag();
                        if (Networking.goBackTo(breadCrumb.getPath()) == Networking.RESULT_OK) {
                            for (int i = mBreadCrumbViews.getChildCount() - 1; i >= 0; i--) {
                                Networking.BreadCrumb testCrumb = (Networking.BreadCrumb) mBreadCrumbViews.getChildAt(i).getTag();
                                if (testCrumb.getPath() == breadCrumb.getPath()) {
                                    populate();
                                    break;
                                } else {
                                    mBreadCrumbViews.removeViewAt(i);
                                }
                            }
                            if (mBreadCrumbViews.getChildCount() == 0) {
                                // TODO error
                            }
                        }
                    }
                });

                mBreadCrumbViews.addView(newCrumb);
            }
        });
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

        //container.setTag(smbFile.getPath());
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (smbFile.isDirectory()) {
                                switch (Networking.goTo(smbFile.getPath())) {
                                    case Networking.RESULT_OK:
                                        addBreadCrumb();
                                        populate();
                                        break;
                                    default:
                                        // TODO handle errors
                                }
                            } else {
                                mCallback.OnShowPhoto(smbFile.getPath());
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
