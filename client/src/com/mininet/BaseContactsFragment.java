package com.mininet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.QuickContactAdapter;
import com.mininet.utils.Utils;

public class BaseContactsFragment extends Fragment {
   public static final String TAG = "BaseContactsActivity";
   protected ListView ContactList;

   protected QuickContactAdapter BadgeAdapter;

   @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
         View v = (View)inflater.inflate(R.layout.contacts_layout, container, false);

         ContactList = (ListView)v.findViewById(R.id.contact_list);

         if (BadgeAdapter != null) {
            ContactList.setAdapter(BadgeAdapter);
            BadgeAdapter.notifyDataSetChanged();
         }
         return v;
      }

   protected void populate(final boolean blocked) {
      Utils.getContactProfilesFromServer(new BaseRequestListener() {
         @Override
         public void onComplete(String response, Object state) {
            Utils.setContactProfiles(response, blocked);
            setAdapter(blocked);
         }
      },
      blocked 
      );
   }

   protected void setAdapter(final boolean blocked) {
      if (getActivity() == null) {
         return;
      }
      getActivity().runOnUiThread(new Runnable(){
         @Override
         public void run() {
            BadgeAdapter = (QuickContactAdapter)Utils.getProfileListAdapter(
               getActivity().getApplicationContext(),
               Utils.getContactProfiles(blocked));
            ContactList.setAdapter(BadgeAdapter);
         }
      });
   }

   @Override
      public void onResume() {
         super.onResume();
         if (ContactList != null) {
            BadgeAdapter = (QuickContactAdapter)ContactList.getAdapter();
            if (BadgeAdapter != null) {
               BadgeAdapter.notifyDataSetChanged();
            }
         }
      }

}



