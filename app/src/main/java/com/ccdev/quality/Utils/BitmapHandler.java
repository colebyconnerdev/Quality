package com.ccdev.quality.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public static int createOrGetThumbnail(String pathToParent, String fileName, Bitmap bitmap) {

        Log.d(TAG, "TEST: createOrGetThumbnail(");  // TODO remove this
        Log.d(TAG, "TEST:   pathToParent = " + pathToParent);   // TODO remove this
        Log.d(TAG, "TEST:   fileName = " + fileName);   // TODO remove this
        Log.d(TAG, "TEST:   <bitmap>);");   // TODO remove this

        int result = getThumbsDir(pathToParent);
        Log.d(TAG, "TEST: getThumbsDir(pathToParent) = " + result); // TODO remove this

        if (result == THUMBS_DIR_EXISTS || result == THUMBS_DIR_CREATED) {
            result = getThumb(pathToParent, fileName, bitmap);
            Log.d(TAG, "TEST: getThumb(pathToParent, fileName, <bitmap>) = " + result); // TODO remove this
        } else if (result == THUMBS_DIR_NOT_CREATED) {
            Log.d(TAG, "TEST: THUMBS_DIR_NOT_CREATED"); // TODO remove this
            return THUMBS_DIR_NOT_CREATED;
        } else {
            Log.d(TAG, "TEST: UNKNOWN_ERROR");  // TODO remove this
            return UNKNOWN_ERROR;
        }

        if (result == THUMB_EXISTS) {
            Log.d(TAG, "TEST: THUMB_EXISTS");   // TODO remove this
            return THUMB_DOWNLOADED;
        } else if (result == THUMB_CREATED) {
            String pathToThumb = pathToParent + "thumbs/" + fileName;
            Log.d(TAG, "TEST: THUMB_CREATED, uploadThumb(");    // TODO remove this
            Log.d(TAG, "TEST:   pathToThumb = " + pathToThumb); // TODO remove this
            return uploadThumb(pathToThumb, bitmap);
        } else if (result == THUMB_NOT_CREATED) {
            Log.d(TAG, "TEST: THUMB_NOT_CREATED");  // TODO remove this
            return THUMB_NOT_CREATED;
        } else if (result == THUMB_NOT_DOWNLOADED) {
            Log.d(TAG, "TEST: THUMB_NOT_DOWNLOADED");   // TODO remove this
            return THUMB_NOT_DOWNLOADED;
        } else {
            Log.d(TAG, "TEST: UNKNOWN_ERROR");  // TODO remove this
            return UNKNOWN_ERROR;
        }
    }

    private static int uploadThumb(String pathToFile, Bitmap bitmap) {
        BufferedOutputStream outputStream;

        try {
            outputStream = new BufferedOutputStream(new SmbFileOutputStream(pathToFile), BUFFER_SIZE);
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
        boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream);

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

    private static int getThumb(String pathToParent, String fileName, Bitmap bitmap) {
        BufferedInputStream inputStream;
        String pathToThumb = pathToParent + "thumbs/" + fileName;
        String pathToOriginal = pathToParent + fileName;

        int status;
        try {
            SmbFile smbFile = new SmbFile(pathToThumb);
            if (!smbFile.exists()) {
                status = THUMB_NOT_EXISTS;
                inputStream = new BufferedInputStream(new SmbFileInputStream(pathToOriginal), BUFFER_SIZE);
            } else {
                status = THUMB_EXISTS;
                inputStream = new BufferedInputStream(new SmbFileInputStream(pathToThumb), BUFFER_SIZE);
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
                inputStream = new BufferedInputStream(new SmbFileInputStream(pathToThumb), BUFFER_SIZE);
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

        bitmap = BitmapFactory.decodeStream(inputStream, null, options);

        try {
            inputStream.close();
        } catch (java.io.IOException e) {
            Log.d(TAG, e.toString());
            // no need to return error
        }

        if (bitmap != null) {
            return (status == THUMB_NOT_EXISTS) ? THUMB_CREATED : THUMB_DOWNLOADED;
        } else {
            return (status == THUMB_NOT_EXISTS) ? THUMB_NOT_CREATED : THUMB_NOT_DOWNLOADED;
        }
    }

    private static int getThumbsDir(String pathToParent) {
        SmbFile parentDir, thumbDir;
        String pathToThumb = pathToParent + "thumbs/";

        try {
            parentDir = new SmbFile(pathToParent);
            if (!parentDir.exists()) {
                return PARENT_DIR_NOT_EXISTS;
            }

            thumbDir = new SmbFile(pathToThumb);
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
            thumbDir.setAttributes(SmbFile.ATTR_HIDDEN);
            thumbDir.mkdir();
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
