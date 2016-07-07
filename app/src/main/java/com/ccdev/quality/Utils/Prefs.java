package com.ccdev.quality.Utils;

import android.content.Context;

import com.ccdev.quality.Ignore.AuthSettings;
import com.securepreferences.SecurePreferences;

/**
 * Created by Coleby on 6/30/2016.
 */

public class Prefs {

    private static final String FILE_NAME = "quality_prefs";
    private static final String SERVER = "server";
    private static final String ROOT = "root";
    private static final String DOMAIN = "domain";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String REMEMBER_ME = "remember_me";

    public static final int SETTINGS_OK = 0;

    public static final int MISSING_DOMAIN = -1;
    public static final int MISSING_SERVER = -2;
    public static final int MISSING_ROOT = -3;

    public static final int MISSING_USERNAME = -1;
    public static final int MISSING_PASSWORD = -2;

    private static SecurePreferences securePreferences;
    private static String server;
    private static String root;
    private static String domain;
    private static String username;
    private static String password;
    private static boolean rememberMe;

    public static void getPrefs(Context context) {
        securePreferences = new SecurePreferences(context, AuthSettings.SECRET_KEY, FILE_NAME);
        server = securePreferences.getString(SERVER, "");
        root = securePreferences.getString(ROOT, "");
        domain = securePreferences.getString(DOMAIN, "");
        username = securePreferences.getString(USERNAME, "");
        password = securePreferences.getString(PASSWORD, "");
        rememberMe = securePreferences.getBoolean(REMEMBER_ME, false);
    }

    public static int checkServerSettings() {

        if (domain == null || domain.isEmpty()) {
            return -1;
        }

        if (server == null || server.isEmpty()) {
            return -2;
        }

        if (root == null || root.isEmpty()) {
            return -3;
        }

        return 0;
    }

    public static int checkUserSettings() {
        if (username == null || username.isEmpty()) {
            return -1;
        }
        if (password == null || password.isEmpty()) {
            return -2;
        }

        return 0;
    }

    public static void resetAll() {
        server = securePreferences.getString(SERVER, "");
        root = securePreferences.getString(ROOT, "");
        domain = securePreferences.getString(DOMAIN, "");
        username = securePreferences.getString(USERNAME, "");
        password = securePreferences.getString(PASSWORD, "");
        rememberMe = securePreferences.getBoolean(REMEMBER_ME, false);
    }

    public static String resetServer() {
        server = securePreferences.getString(SERVER, "");
        return server;
    }

    public static String resetRoot() {
        root = securePreferences.getString(ROOT, "");
        return root;
    }

    public static String restDomain() {
        domain = securePreferences.getString(DOMAIN, "");
        return domain;
    }

    public static String resetUsername() {
        username = securePreferences.getString(USERNAME, "");
        return username;
    }

    public static String resetPassword() {
        password = securePreferences.getString(PASSWORD, "");
        return password;
    }

    public static boolean resetRememberMe() {
        rememberMe = securePreferences.getBoolean(REMEMBER_ME, false);
        return rememberMe;
    }

    public static void commitAll() {
        securePreferences.edit().putString(SERVER, server);
        securePreferences.edit().putString(ROOT, root);
        securePreferences.edit().putString(DOMAIN, domain);
        securePreferences.edit().putString(USERNAME, username);
        securePreferences.edit().putString(PASSWORD, password);
        securePreferences.edit().putBoolean(REMEMBER_ME, rememberMe);
    }

    public static void commitServer() {
        securePreferences.edit().putString(SERVER, server);
    }

    public static void commitRoot() {
        securePreferences.edit().putString(ROOT, root);
    }

    public static void commitDomain() {
        securePreferences.edit().putString(DOMAIN, domain);
    }

    public static void commitUsername() {
        securePreferences.edit().putString(USERNAME, username);
    }

    public static void commitPassword() {
        securePreferences.edit().putString(PASSWORD, password);
    }

    public static void commitRememberMe() {
        securePreferences.edit().putBoolean(REMEMBER_ME, rememberMe);
    }

    public static String getAuthString() {
        // TODO check for null
        return String.format("smb://%s;%s:%s@%s/",domain, username, password, server);
    }

    public static String getServer() {
        return server;
    }

    public static String getRoot() {
        return root;
    }

    public static String getDomain() {
        return domain;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static boolean getRememberMe() {
        return rememberMe;
    }

    public static void setServer(String value) {
        server = value;
    }

    public static void setRoot(String value) {
        root = value;
    }

    public static void setDomain(String value) {
        domain = value;
    }

    public static void setUsername(String value) {
        username = value;
    }

    public static void setPassword(String value) {
        password = value;
    }

    public static void setRememberMe(boolean value) {
        rememberMe = value;
    }
}
