package com.mininet.utils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mininet.R;
import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Location;
import com.mininet.datatypes.Privacy;
import com.mininet.datatypes.Profile;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.listeners.LocationListener;
import com.mininet.listeners.RequestListener;

public class Utils extends Application {
   public static final String TAG = "Utils";
   private static Context context;
   private static SharedPreferences preferences;

   public static final String GCM_SENDER_ID = "1022754125226";


   public static final String [] genders = {
       "Male", "Female" 
   };

   public enum Gender { Male, Female };

   public static String capitalize(String str) {
      return new StringBuffer(str.length())
         .append(Character.toTitleCase(str.charAt(0)))
         .append(str.substring(1).toLowerCase())
         .toString();
   }


   private static Set<Profile> userProfile = Collections.synchronizedSet(new HashSet<Profile>());
   private static Set<Privacy> userPrivacy = Collections.synchronizedSet(new HashSet<Privacy>());
   private static Set<Profile> currentContactProfile = Collections.synchronizedSet(new HashSet<Profile>());
   private static final Map<String, Profile> contactProfiles = Collections.synchronizedMap(new LinkedHashMap<String, Profile>());
   private static final Map<String, Profile> searchedProfiles = Collections.synchronizedMap(new LinkedHashMap<String, Profile>());
   private static final Map<String, Profile> blockedProfiles = Collections.synchronizedMap(new LinkedHashMap<String, Profile>());

   private static final Map<String, Location> locations = Collections.synchronizedMap(new HashMap<String, Location>()); 
   private static final Map<String, LocationListener> locationListeners = Collections.synchronizedMap(new HashMap<String, LocationListener>()); 
   private static final Map<String, Bitmap> bitmaps = Collections.synchronizedMap(new HashMap<String, Bitmap>()); 

   private static final Set<String> interests = Collections.synchronizedSet(new HashSet<String>()); 

   public static Profile getUserProfile() {
      for (Profile p : userProfile) {
         return p;
      }
      return null;
   }

   public static Privacy getUserPrivacy() {
      for (Privacy p : userPrivacy) {
         return p;
      }
      return null;
   }

   public static void setCurrentContactProfile(Profile p) {
      currentContactProfile.clear();
      currentContactProfile.add(p);
   }

   public static Profile getCurrentContactProfile() {
      for (Profile p : currentContactProfile) {
         return p;
      }
      return null;
   }

   public static Map<String, Profile> getContactProfiles(boolean blocked) {
      return !blocked ? contactProfiles : blockedProfiles;
   }

   public static Map<String, Profile> getSearchedProfiles() {
      return searchedProfiles;
   }

   public static boolean isEmpty(EditText etText) {
      return etText.getText().toString().length() == 0;
   }

   public static void toastNotification(Activity act, String msg) {
      Toast t = Toast.makeText(act, msg, Toast.LENGTH_SHORT);
      t.show();
   }

   public static String ListToString(List<String> ss, String delimiter) {
      StringBuffer sb = new StringBuffer();
      for(String s : ss) {
         if (sb.length() > 0) {
            sb.append(delimiter);
         }
         sb.append(s);
      }
      return sb.toString();
   }

   public static Gson GsonProvider() {
      return new GsonBuilder()
         .excludeFieldsWithModifiers(Modifier.TRANSIENT)
         .setDateFormat("yyyy-MM-dd").create();
   }

   @Override
      public void onCreate(){
         super.onCreate();
         context = getApplicationContext();
         preferences = PreferenceManager.getDefaultSharedPreferences(context);
      }

   public static Context getAppContext() {
      return context;
   }

   public static SharedPreferences getPreferences() {
      return preferences;
   }

   public static String serializeProfile(Profile profile) {
      final Set<Profile> profiles = new HashSet<Profile>();
      profiles.add(profile);
      return serializeProfile(profiles);
   }

   public static String serializeProfile(Set<Profile> profiles) {
      return GsonProvider().toJson(profiles);
   }

   public static String serializeUserProfile() {
      return GsonProvider().toJson(userProfile);
   }

   public static Set<Profile> deserializeProfile(String response) {
      Log.d(TAG, response);
      return GsonProvider().fromJson(response, new TypeToken<Set<Profile>>(){}.getType());
   }

   public static String serializePrivacy(Privacy privacy) {
      final Set<Privacy> privacies = new HashSet<Privacy>();
      privacies.add(privacy);
      return serializePrivacy(privacies);
   }

   public static String serializePrivacy(Set<Privacy> privacies) {
      return GsonProvider().toJson(privacies);
   }

   public static String serializeUserPrivacy() {
      return GsonProvider().toJson(userPrivacy);
   }

   public static Set<Privacy> deserializePrivacy(String response) {
      Log.d(TAG, response);
      return GsonProvider().fromJson(response, new TypeToken<Set<Privacy>>(){}.getType());
   }

   public static void setUserProfile(String response) {
      Set<Profile> profiles = deserializeProfile(response);
      userProfile.clear();
      for (Profile p : profiles) {
         userProfile.add(p);
         List<String> profile_interests = p.getInterests();
         if (profile_interests != null) {
            interests.addAll(profile_interests);
            for (String str : profile_interests) {
               String [] strs = str.split(" ");
               interests.addAll(new HashSet<String>(Arrays.asList(strs)));
            }
         }
         break;
      }
   }

   public static void setUserProfile(Profile profile) {
      userProfile.clear();
      userProfile.add(profile);
   }

   public static void setUserPrivacy(String response) {
      Set<Privacy> privacies = deserializePrivacy(response);
      userPrivacy.clear();
      for (Privacy p : privacies) {
         userPrivacy.add(p);
         break;
      }
      setUserPrivacyToPreferences();
   }

   public static void setUserPrivacy(Privacy privacy) {
      userPrivacy.clear();
      userPrivacy.add(privacy);
      setUserPrivacyToPreferences();
   }

   public static void setUserPrivacyToPreferences() {
      Privacy privacy = getUserPrivacy();
      Utils.getPreferences().edit().putInt("privacy_level", privacy.getLevel().ordinal());
      Utils.getPreferences().edit().putBoolean("privacy_basic", privacy.getBasic());
      Utils.getPreferences().edit().putBoolean("privacy_detailed", privacy.getDetailed());
      Utils.getPreferences().edit().putBoolean("privacy_location", privacy.getLocation());
   }

   public static void setContactProfiles(String response, boolean blocked) {
      Set<Profile> profiles = deserializeProfile(response);
      if (profiles == null) {
         return;
      }
      if (!blocked) {
         contactProfiles.clear();
         for (Profile profile : profiles) {
            contactProfiles.put(profile.getId(), profile);
            List<String> profile_interests = profile.getInterests();
            if (profile_interests != null) {
               interests.addAll(profile_interests);
               for (String str : profile_interests) {
                  String [] strs = str.split(" ");
                  interests.addAll(new HashSet<String>(Arrays.asList(strs)));
               }
            }
         }
      }
      else {
         blockedProfiles.clear();
         for (Profile profile : profiles) {
            blockedProfiles.put(profile.getId(), profile);
         }
      }
   }

   public static void setSearchedProfiles(String response) {
      Set<Profile> profiles = deserializeProfile(response);
      if (profiles == null) {
         return;
      }
      searchedProfiles.clear();
      for (Profile profile : profiles) {
         searchedProfiles.put(profile.getId(), profile);
         List<String> profile_interests = profile.getInterests();
         if (profile_interests != null) {
            interests.addAll(profile_interests);
            for (String str : profile_interests) {
               String [] strs = str.split(" ");
               interests.addAll(new HashSet<String>(Arrays.asList(strs)));
            }
         }
      }
   }

   public static void getContactProfilesFromServer(final boolean blocked) {
      getContactProfilesFromServer(
            new BaseRequestListener() {
               @Override
         public void onComplete(String response, Object state) {
            setContactProfiles(response, blocked);
         }},
         blocked
         );
   }

   public static void getContactProfilesFromServer(RequestListener listener, final boolean blocked) {
      Profile profile = getUserProfile();
      if (profile == null) {
         return;
      }
      NetworkUtils.getContacts(
            profile.getId(),
            listener,
            Boolean.toString(blocked)
            );
   }

   public static BaseAdapter getProfileListAdapter(Context context, Map<String, Profile> profiles) {
      List<Map<String, Profile>> data = new ArrayList<Map<String, Profile>>();
      List<Profile> profiles_as_list = new ArrayList<Profile>(profiles.values());
      Collections.sort(profiles_as_list);
      profiles.clear();
      for (Profile profile : profiles_as_list) {
         Map<String, Profile> entry = new HashMap<String, Profile>();
         entry.put( "Profile", profile );
         data.add(entry);
         profiles.put(profile.getId(), profile);
      }
      return new QuickContactAdapter(
            context, 
            data,
            R.layout.contact_item_layout,
            new int [] {R.id.contact_item_subject, R.id.contact_item_avatar});
   }

   public static class Item{
      public final String text;
      public final int icon;
      public final boolean hasIcon;
      public Item(String text, Integer icon) {
         this.text = text;
         this.icon = icon;
         hasIcon = true;
      }
      public Item(String text) {
         this.text = text;
         this.icon = -1;
         hasIcon = false;
      }
      @Override
         public String toString() {
            return text;
         }
      public static ListAdapter getItemListAdapter(final Context context, final Item [] items) {
         return new ArrayAdapter<Item>(
               context,
               android.R.layout.select_dialog_item,
               android.R.id.text1,
               items){
            public View getView(int position, View convertView, ViewGroup parent) {
               //User super class to create the View
               View v = super.getView(position, convertView, parent);
               TextView tv = (TextView)v.findViewById(android.R.id.text1);
               if (items[position].hasIcon) {
                  //Put the image on the TextView
                  tv.setCompoundDrawablesWithIntrinsicBounds(items[position].icon, 0, 0, 0);

                  //Add margin between image and text (support various screen densities)
                  tv.setCompoundDrawablePadding((int) 
                        (5 * context.getResources().getDisplayMetrics().density + 0.5f));
               }
               return v;
            }
         };
      }
   }

   public static void processMessage(String message) {
      Log.d(TAG, "processMessage: " + message);
      try {
         JSONObject jsonObject = new JSONObject(message);
         try {
            JSONArray location_json = jsonObject.getJSONArray("location");
            for (int i = 0; i < location_json.length(); i++) {
               final Location location = GsonProvider().fromJson(location_json.getJSONObject(i).toString(), Location.class);
               setLocation(location.getPid(), location);
               Log.d(TAG, "processMessage: " + locations.toString());
               final LocationListener listener = getLocationListener(location.getPid());
               if (listener != null) {
                  listener.onLocationChanged(location);
               }
            }

         } catch (JSONException e) {}

         try {
            JSONArray interests_json = jsonObject.getJSONArray("interests");
            for (int i = 0; i < interests_json.length(); i++) {
               String interest = interests_json.getString(i);
               interests.add(interest);
               String [] strs = interest.split(" ");
               interests.addAll(new HashSet<String>(Arrays.asList(strs)));
            }
         } catch (JSONException e) {}

      } catch (JSONException e) { 
         Log.e(TAG, e.getMessage());
      }
   }

   public static void setLocation(String id, Location location) {
      locations.put(id, location);
   }

   public static Location getLocation(String id) {
      return locations.get(id);
   }

   public static Location getMyLocation() {
      return getLocation(getUserProfile().getId());
   }

   public static void addLocationListener(String id, LocationListener listener) {
      locationListeners.put(id, listener);
   }

   public static LocationListener getLocationListener(String id) {
      return locationListeners.get(id);
   }

   public static void removeLocationListener(String id) {
      locationListeners.remove(id);
   }

   public static void removeAllLocationListeners() {
      locationListeners.clear();
   }

   public static void addBitmap(String url, Bitmap bitmap) {
      bitmaps.put(url, bitmap);
   }

   public static Bitmap getBitmap(String url) {
      return bitmaps.get(url);
   }

   public static android.location.Location LocationToLocation(Location location) {
      if (location == null) {
         return null;
      }
      android.location.Location l = new android.location.Location("");
      l.setLongitude(location.getLongitude());
      l.setLatitude(location.getLatitude());
      return l;
   }

   public static double distanceBetweenLocations(Location l1, Location l2) {
      if (l1 == null || l2 == null) {
         return Double.POSITIVE_INFINITY;
      }
      return LocationToLocation(l1).distanceTo(LocationToLocation(l2));
   }

   public static Set<String> getInterests() {
      return interests;
   }
}
