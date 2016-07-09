package com.ccdev.quality;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ccdev.quality.Utils.BitmapHandler;

/**
 * Created by Coleby on 7/6/2016.
 */

public class PhotoViewFragment extends BackHandledFragment {
    private OnPhotoViewListener mCallback;

    public static final String IMAGE_PATH = "image_path";

    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private String mImagePath;

    private volatile Thread mDownloadThread;

    public interface OnPhotoViewListener {
        void OnPhotoViewRemove();
        void OnPhotoViewError(int errorCode, String errorMessage);
    }

    @Override
    public boolean onBackPressed() {
        mDownloadThread.interrupt();
        mCallback.OnPhotoViewRemove();
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnPhotoViewListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPhotoViewListener");
        }
    }

    // TODO add error constants
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments().containsKey(IMAGE_PATH)) {
            mImagePath = getArguments().getString(IMAGE_PATH);
            if (mImagePath.isEmpty()) {
                mCallback.OnPhotoViewError(0, "TODO: no path - " + mImagePath);
            }
        } else {
            mCallback.OnPhotoViewError(0, "TODO: no path");
        }

        return inflater.inflate(R.layout.fragment_photoview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mImageView = (ImageView) getView().findViewById(R.id.photo_imageview);
        mProgressBar = (ProgressBar) getView().findViewById(R.id.photo_progressBar);

        startDownload();
    }

    public synchronized void startDownload() {
        mDownloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadImage(BitmapHandler.getFullScaleImage(mImagePath));
            }
        });
        mDownloadThread.start();
    }

    private void loadImage(final Bitmap bitmap) {

        if (bitmap != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                }
            });
        }
    }
}
