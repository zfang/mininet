package com.mininet;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.mininet.utils.QuickContactAdapter;  

import android.content.Intent;

import com.mininet.utils.Utils;

import com.mininet.datatypes.Profile;

import java.util.ArrayList;
import java.util.Collection;

import com.mininet.listeners.LocationListener;

public class ContactsFragment extends BaseContactsFragment implements LocationListener {
   public static final String TAG = "ContactsActivity";

   @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
         View v = super.onCreateView(inflater, container, savedInstanceState);

         ContactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
               Utils.setCurrentContactProfile(new ArrayList<Profile>(Utils.getContactProfiles(false).values()).get(position));
               getActivity().startActivityForResult(new Intent(getActivity().getBaseContext(), ProfileActivity.class)
                  .putExtra(ProfileActivity.TAG, ContactsFragment.TAG)
                  .putExtra("position", position), 0);
            }
         });

         return v;
      }

   @Override
      public void onResume() {
         Log.d(TAG, "onResume");
         super.onResume();
         Collection<Profile> contacts = Utils.getContactProfiles(false).values();
         for (Profile contact : contacts) {
            Utils.addLocationListener(contact.getId(), this);
         }
         populate(false);
      }

   @Override
      public void onPause() {
         Log.d(TAG, "onPause");
         Utils.removeAllLocationListeners();
         super.onPause();
      }

   @Override
      public void onLocationChanged(final com.mininet.datatypes.Location location) {
         if (getActivity() == null) {
            return;
         }
         getActivity().runOnUiThread(new Runnable(){
            @Override
            public void run() {
               if (ContactList != null) {
                  BadgeAdapter = (QuickContactAdapter)ContactList.getAdapter();
                  if (BadgeAdapter != null) {
                     BadgeAdapter.notifyDataSetChanged();
                  }
                  else {
                     BadgeAdapter = (QuickContactAdapter)Utils.getProfileListAdapter(
                        getActivity().getApplicationContext(),
                        Utils.getContactProfiles(false));
                     ContactList.setAdapter(BadgeAdapter);
                  }
               }
            }
         });
      }

}
