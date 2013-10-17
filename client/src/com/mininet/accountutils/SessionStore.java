package com.mininet.accountutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionStore {

    private static final String TOKEN = "access_token";
    private static final String EXPIRES = "expires_in";

    /*
     * Save the access token and expiry date so you don't have to fetch it each
     * time
     */

    public static boolean save(Session session, Context context, String KEY) {
        Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.putString(TOKEN, session.getAccessToken());
        editor.putLong(EXPIRES, session.getAccessExpires());
        return editor.commit();
    }

    /*
     * Restore the access token and the expiry date from the shared preferences.
     */
    public static boolean restore(Session session, Context context, String KEY) {
        SharedPreferences savedSession = context.getSharedPreferences(KEY, Context.MODE_PRIVATE);
        session.setAccessToken(savedSession.getString(TOKEN, null));
        session.setAccessExpires(savedSession.getLong(EXPIRES, 0));
        return session.isSessionValid();
    }

    public static void clear(Context context, String KEY) {
        Editor editor = context.getSharedPreferences(KEY, Context.MODE_PRIVATE).edit();
        editor.clear();
        editor.commit();
    }

}
