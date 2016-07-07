package com.ccdev.quality.Utils;

import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Coleby on 7/1/2016.
 */

public class Networking {

    private static int mStatus = -1;
    private static String mStatusMessage = "";
    public static int SUCCESS = 0;
    public static int ERROR_UNKNOWN = -1;
    public static int ERROR_PREFS_MISSING = -2;
    public static int ERROR_MALFORMED_URL_EXCEPTION = -3;
    public static int ERROR_SMB_RETURNED_NULL = -4;
    public static int ERROR_SMB_EXCEPTION = -5;
    public static int ERROR_PREVIOUS_PATH_NULL = -6;

    private static ArrayList<NetworkLocation> mDirs;
    private static ArrayList<NetworkLocation> mFiles;

    private static Stack<String> mPaths = new Stack<>();
    private static String mCurrentName;

    private static final String FILES_FILTER = "(?i).*(.tif|.tiff|.gif|.jpeg|.jpg|.png)";

    public static int getStatus() {
        return mStatus;
    }

    public static String getStatusMessage() {
        return mStatusMessage;
    }

    private static void setStatus(int status) {
        setStatus(status, "");
    }

    private static void setStatus(int status, String statusMessage) {
        mStatus = status;
        mStatusMessage = statusMessage;
    }

    public static class NetworkLocation {
        public String name = "";
        public String path = "";
        public String details = "";
        boolean isDir = false;

        private NetworkLocation(String name, String path, boolean isDir) {
            this(name, path, "", isDir);
        }

        private NetworkLocation(String name, String path, String details, boolean isDir) {
            this.name = name;
            this.path = path;
            this.details = details;
            this.isDir = isDir;
        }

        public boolean isDir() {
            return isDir;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public String getDetails() {
            return details;
        }
    }

    private static void setAllToNull() {
        mPaths = null;
        mCurrentName = null;
    }

    // TODO better validation
    public static boolean goBack() {
        if (mPaths != null && mPaths.size() > 1) {
            mPaths.pop();
            return getFileTree(mPaths.peek());
        } else {
            setStatus(ERROR_PREVIOUS_PATH_NULL,
                    "Networking.goBack(): no previous path available");
            return false;
        }
    }

    // TODO better validation
    public static boolean goBackTo(String path) {
        if (mPaths != null && mPaths.contains(path)) {
            while (mPaths.peek() != path) {
                mPaths.pop();
            }
            return getFileTree(path);
        } else {
            return false;
        }
    }

    public static boolean getFileTree() {
        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK || Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            setAllToNull();
            setStatus(ERROR_PREFS_MISSING,
                    "Networking.getFileTree(): server and/or user settings not valid");
            return false;
        }

        mPaths = new Stack<>();
        return getFileTree(Prefs.getAuthString() + Prefs.getRoot());
    }

    public static boolean getFileTree(String path) {

        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK || Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            setAllToNull();
            setStatus(ERROR_PREFS_MISSING,
                    "Networking.getFileTree(): server and/or user settings not valid");
            return false;
        }

        // TODO validation on path
        mPaths.push(path);

        SmbFile smbRoot;
        try {
            smbRoot = new SmbFile(path);
        } catch (MalformedURLException e) {
            setAllToNull();
            setStatus(ERROR_MALFORMED_URL_EXCEPTION,
                    "Networking.getFileTree(): malformed url = " + mPaths.peek());
            return false;
        }

        if (smbRoot == null) {
            setAllToNull();
            setStatus(ERROR_SMB_RETURNED_NULL,
                    "Networking.getFileTree(): smbRoot is null");
        }

        mCurrentName = smbRoot.getName();

        mDirs = new ArrayList<>();
        mFiles = new ArrayList<>();

        try {
            for (SmbFile smbFile : smbRoot.listFiles()) {
                if (smbFile.isHidden()) continue;

                if (smbFile.isDirectory()) {
                    mDirs.add(new NetworkLocation(
                            smbFile.getName(), smbFile.getPath(), true));
                }

                if (smbFile.isFile() && smbFile.getName().matches(FILES_FILTER)) {
                    String date =
                            new SimpleDateFormat("MM/dd/yyyy hh:mm aa").format(smbFile.getDate());
                    mFiles.add(new NetworkLocation(
                            smbFile.getName(), smbFile.getPath(), date, false));
                }
            }
        } catch (SmbException e) {
            setAllToNull();
            setStatus(ERROR_SMB_EXCEPTION, "Networking.getFileTree(): SmbException " + e.toString());
            return false;
        }

        setStatus(SUCCESS);
        return true;
    }

    public static String getCurrentPath() {
        // TODO check for null?
        return mPaths.peek();
    }

    public static String getCurrentName() {
        // TODO check for null?
        return mCurrentName;
    }

    public static ArrayList<NetworkLocation> getDirs() {
        return mDirs;
    }

    public static ArrayList<NetworkLocation> getFiles() {
        return mFiles;
    }

    public static ArrayList<NetworkLocation> getDirsFiles() {
        ArrayList<NetworkLocation> newArray = mDirs;
        for (NetworkLocation file : mFiles) {
            newArray.add(file);
        }
        return newArray;
    }
}
