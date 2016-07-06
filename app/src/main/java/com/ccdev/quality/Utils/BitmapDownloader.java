package com.ccdev.quality.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

/**
 * Created by Coleby on 7/4/2016.
 */

public class BitmapDownloader {

    public static void loadBitmap(String pathToFile, ProgressBar loadingView, ImageView imageView, TextView detailsView, int reqWidth, int reqHeight) {
        BitmapWorkerTask task = new BitmapWorkerTask(loadingView, imageView, detailsView, reqWidth, reqHeight);
        task.execute(pathToFile);
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfWidth = width / 2;
            final int halfHeight = height / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

        }

        return inSampleSize;
    }

    private static Bitmap decodeSampledBitmapFromStream(String pathToFile, int reqWidth, int reqHeight) {

        SmbFile smbFile = null;
        BufferedInputStream bufferedInputStream = null;

        try {
            smbFile = new SmbFile(pathToFile);
            bufferedInputStream = new BufferedInputStream(new SmbFileInputStream(smbFile), 4096*4);
            bufferedInputStream.mark(bufferedInputStream.available());
        } catch (java.io.IOException e) {
            Log.d("TEST", e.toString());
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(bufferedInputStream, null, options);

        try {
            bufferedInputStream.reset();
        } catch (java.io.IOException e) {
            Log.d("TEST", e.toString());
            //TODO handle this
            try {
                bufferedInputStream.close();
                bufferedInputStream = new BufferedInputStream(new SmbFileInputStream(smbFile));
            } catch (java.io.IOException e1) {
                Log.d("TEST", e1.toString());
            }
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight) * 2;
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(bufferedInputStream, null, options);
    }

    static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;
        private final WeakReference<ProgressBar> loadingViewWeakReference;
        private final WeakReference<TextView> detailsViewWeakReference;
        private final int reqWidth;
        private final int reqHeight;
        private final int inSampleSize;
        private SmbFileInputStream smbInputStream = null;

        public BitmapWorkerTask(ProgressBar loadingView, ImageView imageView, TextView detailsView, int inSampleSize) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
            loadingViewWeakReference = new WeakReference<ProgressBar>(loadingView);
            detailsViewWeakReference = new WeakReference<TextView>(detailsView);
            this.reqHeight = -1;
            this.reqWidth = -1;
            this.inSampleSize = inSampleSize;
        }

        public BitmapWorkerTask(ProgressBar loadingView, ImageView imageView, TextView detailsView, int reqWidth, int reqHeight) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
            loadingViewWeakReference = new WeakReference<ProgressBar>(loadingView);
            detailsViewWeakReference = new WeakReference<TextView>(detailsView);
            this.reqHeight = reqHeight;
            this.reqWidth = reqWidth;
            this.inSampleSize = -1;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            String pathToFile = params[0];

            Bitmap bitmap = null;

            if (reqWidth > 0 && reqHeight > 0) {
                bitmap = decodeSampledBitmapFromStream(pathToFile, reqWidth, reqHeight);
            } else if (inSampleSize > 0) {
                //bitmap = decodeSampledBitmapFromStream(smbFile, inSampleSize);
                // TODO do I need this?
            } else {
                //TODO shouldn't happen
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = imageViewWeakReference.get();
                final ProgressBar loadingView = loadingViewWeakReference.get();
                final TextView detailsView = detailsViewWeakReference.get();

                if (imageView != null && loadingView != null && detailsView != null) {
                    loadingView.setVisibility(View.GONE);
                    imageView.setImageBitmap(bitmap);

                    int bytes = bitmap.getByteCount();
                    int kb = bytes / 1024;
                    int mb = kb / 1024;
                    int gb = mb / 1024;

                    if (gb != 0) {
                        detailsView.setText(gb + " GB");
                    } else if (mb != 0) {
                        detailsView.setText(mb + " MB");
                    } else if (kb != 0) {
                        detailsView.setText(kb + " KB");
                    } else if (bytes != 0) {
                        detailsView.setText(bytes + " bytes");
                    }
                }
            }
        }
    }
}