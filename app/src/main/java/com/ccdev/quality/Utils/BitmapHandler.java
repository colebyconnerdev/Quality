package com.ccdev.quality.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

/**
 * Created by Coleby on 7/4/2016.
 */

public class BitmapHandler {
    private static final String TAG = "Quality.BitmapHandler";
    private static final int THUMB_WIDTH = 100;
    private static final int THUMB_HEIGHT = 100;

    private static final int BUFFER_SIZE = 4096;
    private static final int JPEG_QUALITY = 80;

    private static final int SMB_EXCEPTION = -1;
    private static final int UNKNOWN_HOST_EXCEPTION = -2;
    private static final int MALFORMED_URL_EXCEPTION = -3;

    private static final int PARENT_DIR_EXISTS = 10;
    private static final int PARENT_DIR_NOT_EXISTS = -10;

    private static final int THUMBS_DIR_EXISTS = 11;
    private static final int THUMBS_DIR_NOT_EXISTS = -11;

    private static final int THUMBS_DIR_CREATED = 12;
    private static final int THUMBS_DIR_NOT_CREATED = -12;

    private static final int THUMB_EXISTS = 13;
    private static final int THUMB_NOT_EXISTS = -13;

    public static final int THUMB_CREATED = 14;
    public static final int THUMB_NOT_CREATED = -14;

    public static final int THUMB_DOWNLOADED = 15;
    public static final int THUMB_NOT_DOWNLOADED = -15;

    public static final int THUMB_UPLOADED = 16;
    public static final int THUMB_NOT_UPLOADED = -16;

    private static final int UNKNOWN_ERROR = -100;

    public static class HandlerItem {
        Bitmap bitmap;
        String pathToParent;
        String pathToThumbDir;
        String pathToFile;
        String pathToThumb;
        String fileName;

        public HandlerItem(String pathToFile) {
            String[] split = pathToFile.split("/");

            this.pathToFile = pathToFile;
            fileName = split[split.length-1];
            pathToParent = pathToFile.replace(fileName, "");
            pathToThumbDir = pathToParent + "thumbs/";
            pathToThumb = pathToThumbDir + fileName;
        }
    }

    public static Bitmap getFullScaleImage(String pathToFile) {
        BufferedInputStream inputStream;

        // TODO validation on all of this

        try {
            SmbFile smbFile = new SmbFile(pathToFile);
            inputStream = new BufferedInputStream(smbFile.getInputStream());
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
            return null;
        } catch (java.io.IOException e) {
            Log.d(TAG, e.toString());
            return null;
        }
        
        return BitmapFactory.decodeStream(inputStream);
    }

    public static Bitmap createOrGetThumbnail(String pathToFile) {

        // TODO validation

        HandlerItem item = new HandlerItem(pathToFile);

        int result = getThumbsDir(item);

        if (result == THUMBS_DIR_EXISTS || result == THUMBS_DIR_CREATED) {
            result = getThumb(item);
        } else if (result == THUMBS_DIR_NOT_CREATED) {
            return null;
        } else {
            return null;
        }

        if (result == THUMB_DOWNLOADED) {
            return item.bitmap;
        } else if (result == THUMB_CREATED) {
            uploadThumb(item); // TODO move this to thread?
            return item.bitmap;
        } else {
            return null;
        }
    }

    private static int uploadThumb(HandlerItem item) {
        BufferedOutputStream outputStream;

        try {
            outputStream = new BufferedOutputStream(new SmbFileOutputStream(item.pathToThumb), BUFFER_SIZE);
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
            return MALFORMED_URL_EXCEPTION;
        } catch (UnknownHostException e ) {
            Log.d(TAG, e.toString());
            return UNKNOWN_HOST_EXCEPTION;
        } catch (SmbException e) {
            Log.d(TAG, e.toString());
            return SMB_EXCEPTION;
        }

        // TODO detect file type
        boolean result = item.bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);

        try {
            outputStream.close();
        } catch (java.io.IOException e) {
            Log.d(TAG, e.toString());
            // no need to alert
        }

        if (result == true) {
            return THUMB_UPLOADED;
        } else {
            return THUMB_NOT_UPLOADED;
        }
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

    private static int getThumb(HandlerItem item) {
        BufferedInputStream inputStream;

        int status;
        try {
            SmbFile smbFile = new SmbFile(item.pathToThumb);
            if (!smbFile.exists()) {
                status = THUMB_NOT_EXISTS;
                inputStream = new BufferedInputStream(new SmbFileInputStream(item.pathToFile), BUFFER_SIZE);
            } else {
                status = THUMB_EXISTS;
                inputStream = new BufferedInputStream(new SmbFileInputStream(item.pathToThumb), BUFFER_SIZE);
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
            return MALFORMED_URL_EXCEPTION;
        } catch (UnknownHostException e ) {
            Log.d(TAG, e.toString());
            return UNKNOWN_HOST_EXCEPTION;
        } catch (SmbException e) {
            Log.d(TAG, e.toString());
            return SMB_EXCEPTION;
        }


        BitmapFactory.Options options = new BitmapFactory.Options();

        if (status == THUMB_NOT_EXISTS) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);

            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, THUMB_WIDTH, THUMB_HEIGHT);

            try {
                inputStream = new BufferedInputStream(new SmbFileInputStream(item.pathToFile), BUFFER_SIZE);
            } catch (MalformedURLException e) {
                Log.d(TAG, e.toString());
                return MALFORMED_URL_EXCEPTION;
            } catch (UnknownHostException e) {
                Log.d(TAG, e.toString());
                return UNKNOWN_HOST_EXCEPTION;
            } catch (SmbException e) {
                Log.d(TAG, e.toString());
                return SMB_EXCEPTION;
            }
        }

        item.bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        try {
            inputStream.close();
        } catch (java.io.IOException e) {
            Log.d(TAG, e.toString());
            // no need to return error
        }

        if (item.bitmap != null) {
            return (status == THUMB_NOT_EXISTS) ? THUMB_CREATED : THUMB_DOWNLOADED;
        } else {
            return (status == THUMB_NOT_EXISTS) ? THUMB_NOT_CREATED : THUMB_NOT_DOWNLOADED;
        }
    }

    private static int getThumbsDir(HandlerItem item) {
        SmbFile parentDir, thumbDir;

        try {
            parentDir = new SmbFile(item.pathToParent);
            if (!parentDir.exists()) {
                return PARENT_DIR_NOT_EXISTS;
            }

            thumbDir = new SmbFile(item.pathToThumbDir);
            if (thumbDir.exists()) {
                return THUMBS_DIR_EXISTS;
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, e.toString());
            return MALFORMED_URL_EXCEPTION;
        } catch (SmbException e) {
            Log.d(TAG, e.toString());
            return SMB_EXCEPTION;
        }

        try {
            thumbDir.mkdir();
            thumbDir.setAttributes(SmbFile.ATTR_HIDDEN);
        } catch (SmbException e) {
            Log.d(TAG, e.toString());
            return SMB_EXCEPTION;
        }

        try {
            if (thumbDir.exists()) {
                return THUMBS_DIR_CREATED;
            } else {
                return THUMBS_DIR_NOT_CREATED;
            }
        } catch (SmbException e) {
            Log.d(TAG, e.toString());
            return SMB_EXCEPTION;
        }
    }
}
