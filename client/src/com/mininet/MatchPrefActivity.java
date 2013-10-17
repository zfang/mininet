package com.mininet;

import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.mininet.utils.Utils;

public class MatchPrefActivity extends PreferenceActivity {

   public static final String TAG = "MatchPrefActivity";

   private static MatchPrefActivity instance = null;

   public static MatchPrefActivity getInstance() {
      return instance;
   }

   @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         PreferenceManager.setDefaultValues(this, R.xml.preference_layout, false);
         addPreferencesFromResource(R.xml.preference_layout);
         instance = this;
         // Init
         PreferenceScreen prefScreen = getPreferenceScreen();
         for (int i = prefScreen.getPreferenceCount() - 1; i >= 0; --i) {
            Preference pref = prefScreen.getPreference(i);
            ListPreference listPref = (ListPreference)pref;
            if (listPref.getValue().equals("2")) {
               String value = Utils.getPreferences().getString(listPref.getKey()+"_custom", "");
               listPref.setSummary(value);
            }
            else {
               listPref.setSummary(listPref.getEntry());
            }
         }
      }

}


