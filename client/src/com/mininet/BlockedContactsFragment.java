package com.mininet;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
   
import android.content.Intent;

import com.mininet.utils.Utils;

import com.mininet.datatypes.Profile;

import java.util.ArrayList;

public class BlockedContactsFragment extends BaseContactsFragment {
   public static final String TAG = "BlockedContactsActivity";

   @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
         View v = super.onCreateView(inflater, container, savedInstanceState);

         ContactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
               Utils.setCurrentContactProfile(new ArrayList<Profile>(Utils.getContactProfiles(true).values()).get(position));
               getActivity().startActivityForResult(new Intent(getActivity(), ProfileActivity.class)
                  .putExtra(ProfileActivity.TAG, BlockedContactsFragment.TAG)
                  .putExtra("position", position), 0);
            }
         });

         return v;
      }

   @Override
      public void onResume() {
         Log.d(TAG, "onResume");
         super.onResume();
         populate(true);
      }

}
