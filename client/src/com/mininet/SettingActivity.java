package com.mininet;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.Utils;
import com.mininet.client2server.NetworkUtils;

import android.util.Log;

import com.mininet.datatypes.Privacy;

public class SettingActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

   public static final String TAG = "SettingActivity";

   private boolean privacyChanged = false;

   public static final String level_key = "level";
   public static final String basic_key = "basic";
   public static final String detailed_key = "detailed";
   public static final String location_key = "location";

   private static final String privacy_level_key = "privacy_" + level_key;
   private static final String privacy_basic_key = "privacy_" + basic_key;
   private static final String privacy_detailed_key = "privacy_" + detailed_key;
   private static final String privacy_location_key = "privacy_" + location_key;

   @SuppressWarnings("deprecation")
      @Override
      public void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         PreferenceManager.setDefaultValues(this, R.xml.setting_layout, false);
         addPreferencesFromResource(R.xml.setting_layout);
         OnPreferenceChangeListener changedListener = new OnPreferenceChangeListener() {

            @Override
               public boolean onPreferenceChange(Preference preference,
                     Object newValue) {
                  privacyChanged = true;
                  return true;
               }
         };
         findPreference(privacy_level_key).setOnPreferenceChangeListener(changedListener);
         findPreference(privacy_basic_key).setOnPreferenceChangeListener(changedListener);
         findPreference(privacy_detailed_key).setOnPreferenceChangeListener(changedListener);
         findPreference(privacy_location_key).setOnPreferenceChangeListener(changedListener);
      }

   @SuppressWarnings("deprecation")
      @Override
      protected void onResume() {
         super.onResume();
         getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

         // A patch to overcome OnSharedPreferenceChange not being called by RingtonePreference bug 
         //((RingtonePreference)findPreference("setting_sound")).setOnPreferenceChangeListener(this);
      }

   @SuppressWarnings("deprecation")
      @Override
      protected void onPause() {
         super.onPause();
         getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
         updatePrivacyToServer();
      }

   @Override
      public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
         final Preference pref = findPreference(key);

         if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
         }

      }

   @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
         // TODO
         //updateRingtoneSummary((RingtonePreference) preference, Uri.parse((String) newValue));
         return true;
      }

   private void updateRingtoneSummary(RingtonePreference preference, Uri ringtoneUri) {
      Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
      if (ringtone != null)
         preference.setSummary(ringtone.getTitle(this));
      else
         preference.setSummary("Silent");
   }

   private void updatePrivacyToServer() {
      Log.d(TAG, "updatePrivacyToServer");
      if (!privacyChanged) {
         return;
      }
      Privacy privacy = Utils.getUserPrivacy();
      int level = Integer.parseInt(Utils.getPreferences().getString(privacy_level_key, ""));
      privacy.setLevel(Privacy.Level.values()[level]);
      boolean basic = Utils.getPreferences().getBoolean(privacy_basic_key, false);
      privacy.setBasic(basic);
      boolean detailed = Utils.getPreferences().getBoolean(privacy_detailed_key, false);
      privacy.setDetailed(detailed);
      boolean location = Utils.getPreferences().getBoolean(privacy_location_key, false);
      privacy.setLocation(location);
      Log.d(TAG, String.format("level: %d, basic: %b, detailed: %b, location: %b", 
               level, basic, detailed, location));
      NetworkUtils.setPrivacy(Utils.serializeUserPrivacy(), new BaseRequestListener() {
         @Override
         public void onComplete(String response, Object state) {
            SettingActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  Utils.toastNotification(SettingActivity.this, "Privacy setting is updated.");
               }
            });
         }
      });
      privacyChanged = false;
   }

}
