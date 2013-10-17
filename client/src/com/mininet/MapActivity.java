package com.mininet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.listeners.LocationListener;
import com.mininet.utils.Utils;

public class MapActivity extends FragmentActivity
   implements LocationListener, OnMyLocationChangeListener {

   public static final String TAG = "MapActivity";

   /** Demonstrates customizing the info window and/or its contents. */
   class CustomInfoWindowAdapter implements InfoWindowAdapter {

      private final View mWindow;

      CustomInfoWindowAdapter() {
         mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
      } 

      @Override
         public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
         }

      @Override
         public View getInfoContents(Marker marker) {
            return null;
         }

      private void render(Marker marker, View view) {
         Log.d(TAG, "render: marker_id "+marker.getId());
         //ImageView badge = ((ImageView) view.findViewById(R.id.badge));
         //Profile contact = Utils.getContactProfiles(false).get(mMarkersContacts.get(marker.getId()));
         //if (contact != null) {
         //   Log.d(TAG, "render: contact is not null");
         //   Bitmap bitmap = Utils.getBitmap(contact.getAvatar());
         //   if (bitmap != null) {
         //      badge.setImageBitmap(bitmap);
         //   }
         //   else {
         //      badge.setTag(contact.getAvatar());
         //      new NetworkUtils.DownloadImagesTask().execute(badge);
         //   }
         //}

         String title = marker.getTitle();
         TextView titleUi = ((TextView) view.findViewById(R.id.title));
         if (title != null) {
            // Spannable string allows us to edit the formatting of the text.
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0, titleText.length(), 0);
            titleUi.setText(titleText);
         } else {
            titleUi.setText("");
         }

         String snippet = marker.getSnippet();
         TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
         if (snippet != null && snippet.length() > 12) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, 8, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE), 10, snippet.length(), 0);
            snippetUi.setText(snippetText);
         } else {
            snippetUi.setText("");
         }
      }
   }

   private GoogleMap mMap;
   private UiSettings mUiSettings;

   private Map<String, Marker> mContacts = new HashMap<String, Marker>();
   private Map<String, String> mMarkersContacts = new HashMap<String, String>();

   @Override
      protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.map_layout);

         setUpMapIfNeeded();
      }

   @Override
      protected void onResume() {
         super.onResume();
         setUpMapIfNeeded();
         addMarkersToMap();
         panMarkersInView();
      }

   @Override
      protected void onPause() {
         super.onPause();
         Utils.removeAllLocationListeners();
      }

   private void setUpMapIfNeeded() {
      // Do a null check to confirm that we have not already instantiated the map.
      if (mMap == null) {
         // Try to obtain the map from the SupportMapFragment.
         mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
            .getMap();
         // Check if we were successful in obtaining the map.
         if (mMap != null) {
            setUpMap();
         }
      }
   }

   private void setUpMap() {
      mUiSettings = mMap.getUiSettings();

      // Hide the zoom controls as the button panel will cover it.
      mMap.getUiSettings().setZoomControlsEnabled(false);
      mUiSettings.setMyLocationButtonEnabled(true);
      mUiSettings.setCompassEnabled(true);
      mMap.setMyLocationEnabled(true);
      mMap.setIndoorEnabled(true);

      // Add lots of markers to the map.
      addMarkersToMap();

      // Setting an info window adapter allows us to change the both the contents and look of the
      // info window.
      mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
   }

   private void addMarkersToMap() {
      mMap.clear();
      mContacts.clear();
      mMarkersContacts.clear();
      Utils.removeAllLocationListeners();

      Profile currentContact = Utils.getCurrentContactProfile();
      Map<String, Profile> contacts = Utils.getContactProfiles(false);

      // Single mode
      if (currentContact != null) {
         Utils.addLocationListener(currentContact.getId(), this);
         addMarker(currentContact);
      }
      // Multi mode
      else {
         for (Profile contact : contacts.values()) {
            Utils.addLocationListener(contact.getId(), this);
            addMarker(contact);
         }
      }

   }

   private Bitmap getBitmap(Profile contact) {
      Bitmap bitmap = Utils.getBitmap(contact.getAvatar());
      if (bitmap == null) {
         return null;
      }

      Bitmap backGround = BitmapFactory.decodeResource(getResources(), R.drawable.custom_info_bubble);
      backGround = Bitmap.createScaledBitmap(
                     backGround, (int)(bitmap.getWidth()*1.3), (int)(bitmap.getHeight()*1.9), true);
      Canvas canvas = new Canvas(backGround);
      canvas.drawBitmap(bitmap, (int)(backGround.getWidth()*.1), (int)(backGround.getHeight()*.08), null);
      return backGround;
   }

   private void addMarker(final Profile contact) {
      Bitmap bitmap = getBitmap(contact);
      if (bitmap == null) {
         new NetworkUtils.DownloadImagesTask().execute(null, contact.getAvatar(),
               new BaseRequestListener() {
                  @Override
            public void onComplete(String response, Object state) {
               MapActivity.this.runOnUiThread(new Runnable() {
                  public void run() {
                     addMarker(contact);
                  }
               });
            }});
         return;
      }
      Marker marker = null;
      LatLng ll = LocationToLatLng(Utils.getLocation(contact.getId()));
      if (ll != null) {
         marker = mMap.addMarker(new MarkerOptions()
               .position(ll)
               .title(contact.getUsername())
               .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
         mContacts.put(contact.getId(), marker); 

         if (marker != null) {
            mMarkersContacts.put(marker.getId(), contact.getId());
         }
      }
   }

   private void panMarkersInView() {
      LatLng myLL = LocationToLatLng(mMap.getMyLocation());
      if (myLL == null) {
         myLL = LocationToLatLng(Utils.getMyLocation());
         if (myLL == null) {
            return;
         }
      }
      final List<LatLng> lls = new LinkedList<LatLng>();
      lls.add(myLL);
      for (Marker marker : mContacts.values()) {
         marker.setSnippet("Distance: "+String.format("%.2fm", 
                  LatLngToLocation(myLL).distanceTo(LatLngToLocation(marker.getPosition()))));
         lls.add(marker.getPosition());
      }

      // Pan to see all markers in view.
      // Cannot zoom to bounds until the map has a size.
      FragmentManager manager = getSupportFragmentManager();
      if (manager == null) {
         return;
      }
      Fragment fragment = manager.findFragmentById(R.id.map);
      if (fragment == null) {
         return;
      }
      final View mapView = fragment.getView();
      if (mapView.getViewTreeObserver().isAlive()) {
         mapView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation") // We use the new method when supported
            @SuppressLint("NewApi") // We check which build version we are using.
            @Override
            public void onGlobalLayout() {
               LatLngBounds.Builder builder = new LatLngBounds.Builder();
               for (LatLng ll : lls) {
                  builder.include(ll);
               }
               LatLngBounds bounds = builder.build();
               if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                  mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
               } else {
                  mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
               mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
            }
         });
      }

   }

   @Override
      public void onMyLocationChange(final Location location) {
         panMarkersInView();
      }

   @Override
      public void onLocationChanged(final com.mininet.datatypes.Location location) {
         this.runOnUiThread(new Runnable() {
            public void run() {
               Log.d(TAG, "onLocationChanged from "+location.getPid());
               LatLng ll = LocationToLatLng(location);
               Profile contact = Utils.getContactProfiles(false).get(location.getPid());
               if (contact == null) {
                  Log.d(TAG, "onLocationChanged from "+location.getPid()+": contact is null");
                  return;
               }
               Marker marker = mContacts.get(location.getPid());
               if (marker != null) {
                  marker.setPosition(ll);
               }
               else {
                  Bitmap bitmap = getBitmap(contact);
                  marker = mMap.addMarker(new MarkerOptions()
                     .position(ll)
                     .title(contact.getUsername())
                     .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                     //.icon(BitmapDescriptorFactory.defaultMarker(mContacts.size() * 360 / Utils.getContactProfiles(false).size())));
                  mContacts.put(location.getPid(), marker);
                  if (marker != null) {
                     Log.d(TAG, "onLocationChanged from "+location.getPid()+": new marker is not null");
                     mMarkersContacts.put(marker.getId(), contact.getId());
                  }
               }

         panMarkersInView();
            }
         });
      }

   public static Location LatLngToLocation(LatLng ll) {
      if (ll == null) {
         return null;
      }
      Location location = new Location("");
      location.setLatitude(ll.latitude);
      location.setLongitude(ll.longitude);
      return location;
   }

   public static LatLng LocationToLatLng(Location location) {
      if (location == null) {
         return null;
      }
      return new LatLng(location.getLatitude(), location.getLongitude());
   }

   public static LatLng LocationToLatLng(com.mininet.datatypes.Location location) {
      if (location == null) {
         return null;
      }
      return new LatLng(location.getLatitude(), location.getLongitude());
   }
}

