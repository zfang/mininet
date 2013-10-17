package com.mininet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.QuickContactAdapter;
import com.mininet.utils.Utils;
import com.mininet.utils.Utils.Gender;

public class SearchFragment extends Fragment {
   public static final String TAG = "SearchFragment";

   public enum SearchKey {
      gender,
      birthdate,
      interests,
   };

   ListView SearchResult;
   Button SearchButton;
   BaseAdapter SearchAdapter;

   @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
         Log.d(TAG, "onCreateView");
         View v = (View)inflater.inflate(R.layout.search_result, container, false);

         SearchResult = (ListView)v.findViewById(R.id.searchResult);
         SearchResult.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
               Log.d(TAG, "onItemClick: position" + position);
               Log.d(TAG, "onItemClick: " + Utils.getSearchedProfiles());
               Utils.setCurrentContactProfile(new ArrayList<Profile>(Utils.getSearchedProfiles().values()).get(position));
               getActivity().startActivityForResult(new Intent(getActivity().getBaseContext(), ProfileActivity.class)
                  .putExtra(ProfileActivity.TAG, SearchFragment.TAG)
                  .putExtra("position", position), 0);
            }
         });

         if (SearchAdapter != null) {
            SearchResult.setAdapter(SearchAdapter);
            SearchAdapter.notifyDataSetChanged();
         }

         SearchButton = (Button)v.findViewById(R.id.b_search);
         SearchButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
               search();
            }
         });

         return v;
      }
   @Override
      public void onResume() {
         super.onResume();
         if (SearchResult != null) {
            QuickContactAdapter adapter = (QuickContactAdapter)SearchResult.getAdapter();
            if (adapter != null) {
               adapter.notifyDataSetChanged();
            }
         }
      }

   private void search() {
      SearchButton.setEnabled(false);
      SearchButton.setText("Loading...");
      Map<String, String> queries = new HashMap<String, String>();
      SharedPreferences prefs = Utils.getPreferences();
      Profile user = Utils.getUserProfile();
      for (int i = 0; i < SearchKey.values().length; ++i) {
         String value = prefs.getString("match_"+SearchKey.values()[i], "");
         if (value == "") {
            continue;
         }
         SearchKey key = SearchKey.values()[i];
         switch (Integer.parseInt(value)) {
            case 0:
               // skip
               break;
            case 1:
               switch (key) {
                  case gender:
                     queries.put(key.name(), Gender.values()[1 ^ Gender.valueOf(user.getGender()).ordinal()].name());
                     break;
                  case birthdate:
                     queries.put(key.name(), user.getBirthdate().toString());
                     break;
                  case interests:
                     List<String> interests = user.getInterests();
                     if (interests != null && !interests.isEmpty()) {
                        queries.put(
                              key.name(),
                              Utils.ListToString(
                                 interests,
                                 ".")
                              );
                     }
                     break;
               }
               break;
            case 2:
               String q = prefs.getString("match_"+key.name()+"_custom", "");
               Log.d(TAG, "q: " + q);
               if (!q.equals("")) {
                  if (key.equals(SearchKey.interests)) {
                     String [] tokens = q.split("\\s*,\\s*");
                     q = Utils.ListToString(Arrays.asList(tokens), ".");
                     Log.d(TAG, "q: " + q);
                  }
                  queries.put(key.name(), q);
               }
               break;
         }
         Log.d(TAG, key.name() + " in preference: " +  prefs.getString("match_"+key.name()+"_custom", ""));
      }
      NetworkUtils.search(
            user.getId(),
            queries, 
            new BaseRequestListener() {
               @Override
         public void onComplete(final String response, Object state) {
            Utils.setSearchedProfiles(response);
            getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  SearchButton.setText(getString(R.string.search_button));
                  SearchButton.setEnabled(true);
                  SearchAdapter = 
               Utils.getProfileListAdapter(
                  getActivity().getApplicationContext(),
                  Utils.getSearchedProfiles());
            SearchResult.setAdapter(SearchAdapter);
               }
            });
         }
         });
   }

}
