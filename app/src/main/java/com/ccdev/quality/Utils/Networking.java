package com.ccdev.quality.Utils;

import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by Coleby on 7/1/2016.
 */

public class Networking {

    public static final int RESULT_OK = 0;

    private static ArrayList<SmbFile> mDirs;
    private static ArrayList<SmbFile> mFiles;

    private static String mCurrentPath;
    private static String mCurrentName;

    private static final String FILES_FILTER = "(?i).*(.tif|.tiff|.gif|.jpeg|.jpg|.png)";

    public static String getCurrentPath() {
        // TODO check for null?
        return mCurrentPath;
    }

    public static String getCurrentName() {
        // TODO check for null?
        return mCurrentName;
    }

    public static int goBack() {
        String newPath = mCurrentPath.replace(mCurrentName, "");

        // TODO validate

        if (newPath.length() < Prefs.getRoot().length()) {
            // TODO handle this
            return -1;
        }

        return getFileTree(newPath);
    }

    public static int getFileTree(String path) {

        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK || Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            // TODO handle this
        }

        mCurrentPath = path;

        SmbFile smbFiles = null;
        try {
            smbFiles = new SmbFile(path);
        } catch (MalformedURLException e) {
            // TODO handle this
        }

        if (smbFiles == null) {
            // TODO handle this
        }

        mCurrentName = smbFiles.getName();
        mDirs = new ArrayList<>();
        mFiles = new ArrayList<>();

        try {
            for (SmbFile smbFile : smbFiles.listFiles()) {
                if (smbFile.isHidden()) continue;
                if (smbFile.isDirectory()) mDirs.add(smbFile);
                if (smbFile.isFile() && smbFile.getName().matches(FILES_FILTER)) mFiles.add(smbFile);     // TODO this doesn't work.
            }
        } catch (SmbException e) {
            // TODO handle this
        }

        return RESULT_OK;

    }

    public static int getInitialFileTree() {

        if (Prefs.checkServerSettings() != Prefs.SETTINGS_OK || Prefs.checkUserSettings() != Prefs.SETTINGS_OK) {
            // TODO handle this
        }

        mCurrentPath = String.format("smb://%s;%s:%s@%s/%s/",
                Prefs.getDomain(),
                Prefs.getUsername(),
                Prefs.getPassword(),
                Prefs.getServer(),
                Prefs.getRoot());

        SmbFile smbFiles = null;
        try {
            smbFiles = new SmbFile(mCurrentPath);
        } catch (MalformedURLException e) {
            // TODO handle this
        }

        if (smbFiles == null) {
            // TODO handle this
        }
        mCurrentName = smbFiles.getName();

        mDirs = new ArrayList<>();
        mFiles = new ArrayList<>();

        try {
            for (SmbFile smbFile : smbFiles.listFiles()) {
                if (smbFile.isHidden()) continue;
                if (smbFile.isDirectory()) mDirs.add(smbFile);
                if (smbFile.isFile() && smbFile.getName().matches(FILES_FILTER)) mFiles.add(smbFile);
            }
        } catch (SmbException e) {
            // TODO handle this
        }

        return RESULT_OK;
    }

    public static ArrayList<SmbFile> getDirs() {
        // TODO if null? sort?
        return mDirs;
    }

    public static ArrayList<SmbFile> getFiles() {
        // TODO if null? sort?
        return mFiles;
    }

    public static ArrayList<SmbFile> getDirsFiles() {
        ArrayList<SmbFile> newArray = mDirs;
        newArray.addAll(mFiles);

        return newArray;
    }
}
