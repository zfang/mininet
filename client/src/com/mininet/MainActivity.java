package com.mininet;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewConfiguration;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;
import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Location;
import com.mininet.datatypes.Profile;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.Utils;

public class MainActivity extends SherlockFragmentActivity 
   implements LocationListener, OnSharedPreferenceChangeListener {

   public static final String TAG = "MainActivity";

   ViewPager  mViewPager;
   TabsAdapter mTabsAdapter;

   private void forceOverflow() {
      try {
         ViewConfiguration config = ViewConfiguration.get(this);
         Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
         if(menuKeyField != null) {
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
         }
      } catch (Exception ex) {}

   }

   private void GCMRegister() {
      GCMRegistrar.checkDevice(this);
      GCMRegistrar.checkManifest(this);
      String regId = GCMRegistrar.getRegistrationId(this);
      if (regId.equals("")) {
         GCMRegistrar.register(this, Utils.GCM_SENDER_ID);
      }
      else {
         if (!GCMRegistrar.isRegisteredOnServer(this)) {
            NetworkUtils.registerGCM(
                  Utils.getUserProfile().getId(),
                  regId,
                  new BaseRequestListener() {
                     @Override
               public void onComplete(String response, Object state) {
                  GCMRegistrar.setRegisteredOnServer(MainActivity.this, true);
               }
               });
         }
      }
   }

   @Override
      public void onLocationChanged(android.location.Location location) {
         // Called when a new location is found by the network location provider.
         Profile user = Utils.getUserProfile();
         if (user == null) {
            return;
         }
         Location data = new Location();
         data.setPid(user.getId());
         data.setLatitude(location.getLatitude());
         data.setLongitude(location.getLongitude());
         Utils.setLocation(data.getPid(), data);
         NetworkUtils.setLocation(
               Utils.GsonProvider().toJson(data),
               new BaseRequestListener() {
                  @Override
            public void onComplete(String response, Object state) {
               try {
                  String errMsg = new JSONObject(response).getString("error");
                  Utils.toastNotification(MainActivity.this, errMsg);
               } catch (JSONException e) {
               }
            }
            });
      }

   @Override
      public void onStatusChanged(String provider, int status, Bundle extras) {}

   @Override
      public void onProviderEnabled(String provider) {}

   @Override
      public void onProviderDisabled(String provider) {}

   public void locationUpdateEnable() {
      Log.d(TAG, "locationUpdateEnable");
      // Acquire a reference to the system Location Manager
      LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

      Criteria criteria = new Criteria();
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
      criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);

      // Register the listener with the Location Manager to receive location updates
      locationManager.requestLocationUpdates(0, 1, criteria, this, null);
   }

   public void locationUpdateDisable() {
      Log.d(TAG, "locationUpdateDisable");
      LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
      locationManager.removeUpdates(this);
   }

   @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (Utils.getUserProfile() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
         }

         setContentView(R.layout.main_page_layout);

         ActionBar bar = getSupportActionBar();
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

         mViewPager = (ViewPager)findViewById(R.id.viewpager);

         // Add the tabs
         mTabsAdapter = new TabsAdapter(this, bar, mViewPager);
         mTabsAdapter.addTab(bar.newTab().setText("Search"), SearchFragment.class, null);
         mTabsAdapter.addTab(bar.newTab().setText("Contacts"), ContactsFragment.class, null);
         mTabsAdapter.addTab(bar.newTab().setText("Profile"), UserInfoFragment.class, null);
         mTabsAdapter.addTab(bar.newTab().setText("Blocked"), BlockedContactsFragment.class, null);

         setTitle(R.string.app_name);

         if (savedInstanceState != null) {
            bar.setSelectedNavigationItem(savedInstanceState.getInt("tab"));
         }

         forceOverflow();
         GCMRegister();
         if (Utils.getPreferences().getBoolean("privacy_location", true)) {
            locationUpdateEnable();
         }
         else {
            locationUpdateDisable();
         }

         Utils.getPreferences().registerOnSharedPreferenceChangeListener(this);
      }

   @Override
      protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("tab", getSupportActionBar().getSelectedNavigationIndex());
      }

   public static class TabsAdapter extends FragmentPagerAdapter
         implements ViewPager.OnPageChangeListener, ActionBar.TabListener {
         private final Context mContext;
         private final ActionBar mBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

         static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;

            TabInfo(Class<?> _class, Bundle _args) {
               clss = _class;
               args = _args;
            }
         }

         public TabsAdapter(FragmentActivity activity, ActionBar bar, ViewPager pager) {
            super(activity.getSupportFragmentManager());
            mContext = activity;
            mBar = bar;
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
         }

         public void addTab(ActionBar.Tab tab, Class<? extends Fragment> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mBar.addTab(tab);
            notifyDataSetChanged();
         }

         @Override
            public int getCount() {
               return mTabs.size();
            }

         @Override
            public Fragment getItem(int position) {
               TabInfo info = mTabs.get(position);
               return Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }

         @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

         @Override
            public void onPageSelected(int position) {
               mBar.setSelectedNavigationItem(position);
            }

         @Override
            public void onPageScrollStateChanged(int state) {
            }

         @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) {
               Object tag = tab.getTag();
               for (int i=0; i<mTabs.size(); i++) {
                  if (mTabs.get(i) == tag) {
                     mViewPager.setCurrentItem(i);
                  }
               }
            }

         @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) {

            }

         @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {

            }
   }

   @Override
      public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.pref_setting_menu, menu);
         return true;
      }

   @Override
      public boolean onOptionsItemSelected(MenuItem item) {
         boolean result = super.onOptionsItemSelected(item);
         switch (item.getItemId()) {
            case R.id.menu_log_out:
               logout();
               return true;
            case R.id.menu_search_pref:
               startActivity(new Intent(this, MatchPrefActivity.class));
               return true;
            case R.id.menu_setting:
               startActivity(new Intent(this, SettingActivity.class));
               return true;
            case R.id.menu_locations:
               startActivity(new Intent(this, MapActivity.class));
               return true;
            default:
               return result;
         }
      }



   private void logout() {
      startActivityForResult(new Intent(this, AccountMgmtActivity.class), 
            AccountMgmtActivity.CODE_LOGOUT);
   }

   @Override
      public void onResume() {
         Log.d(TAG, "onResume");
         super.onResume();
         extend();
      }

   @Override
      public void startActivityForResult(Intent intent, int requestCode) {
         intent.putExtra("requestCode", requestCode);
         super.startActivityForResult(intent, requestCode);
      }

   @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Log.d(TAG, "onActivityResult");
         if (resultCode == RESULT_OK) {
            switch (requestCode) {
               case AccountMgmtActivity.CODE_LOGOUT:
                  startActivity(new Intent(this, LoginActivity.class));
                  locationUpdateDisable();
                  finish();
                  return;
            }
            if (data == null) {
               return;
            }
            final int code = data.getIntExtra(ProfileActivity.TAG, -1); 
            final int position = data.getIntExtra("position", -1); 
            Log.d(TAG, Integer.toString(code));
            Log.d(TAG, Integer.toString(position));
            Profile user = Utils.getUserProfile();
            Profile contact = Utils.getCurrentContactProfile();
            switch (code) {
               case ProfileActivity.CODE_ADD:
                  Utils.getContactProfiles(false).put(contact.getId(), contact);
                  NetworkUtils.addContacts(user.getId(),
                        contact.getId(),
                        new BaseRequestListener() {
                           @Override
                     public void onComplete(final String response, Object state) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                           public void run() {
                           }
                        });
                     }
                     });
                  break;
               case ProfileActivity.CODE_DELETE:
                  Utils.getContactProfiles(false).remove(contact.getId());
                  NetworkUtils.deleteContacts(user.getId(),
                        contact.getId(),
                        new BaseRequestListener() {
                           @Override
                     public void onComplete(final String response, Object state) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                           public void run() {
                           }
                        });
                     }
                     });
                  break;
               case ProfileActivity.CODE_BLOCK:
                  Utils.getContactProfiles(true).put(contact.getId(), contact);
                  Utils.getContactProfiles(false).remove(contact.getId());
                  NetworkUtils.blockContacts(user.getId(),
                        contact.getId(),
                        new BaseRequestListener() {
                           @Override
                     public void onComplete(final String response, Object state) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                           public void run() {
                           }
                        });
                     }
                     });
                  break;
               case ProfileActivity.CODE_UNBLOCK:
                  Utils.getContactProfiles(true).remove(contact.getId());
                  Utils.getContactProfiles(false).put(contact.getId(), contact);
                  NetworkUtils.unblockContacts(user.getId(),
                        contact.getId(),
                        new BaseRequestListener() {
                           @Override
                     public void onComplete(final String response, Object state) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                           public void run() {
                           }
                        });
                     }
                     });
                  break;
            }
         }
      }

   protected void extend() {
      if (!AccountMgmtActivity.extend()) {
         Utils.toastNotification(this, "You are no longer logged in!");
         startActivity(new Intent(this, LoginActivity.class));
         finish();
      }
   }

   @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         Log.d(TAG, "onSharedPreferenceChanged: key: "+key);
         if (key.equals("privacy_location")){
            locationUpdateDisable();
            if (Utils.getPreferences().getBoolean(key, true)) {
               locationUpdateEnable();
            }
         }
      }

}
