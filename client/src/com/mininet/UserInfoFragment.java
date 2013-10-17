package com.mininet;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.QuickContactBadge;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.Utils;

public class UserInfoFragment extends Fragment {
   public static final String TAG = "UserInfoFragment";
   public static final int CODE_REGISTER = 0;
   public static final int CODE_UPDATE = 1;

   private int currentCode;

   EditText EditUsername;
   RadioGroup RadioGender;
   TextView TextBirthdate;
   EditText EditEmail;
   MultiAutoCompleteTextView EditInterests;

   public static UserInfoFragment newInstance(int code) {
      UserInfoFragment userFrag = new UserInfoFragment();
      Bundle bundle = new Bundle();
      bundle.putInt(TAG, code);
      userFrag.setArguments(bundle);
      return userFrag;
   }


   @SuppressLint("NewApi")
      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
         Log.d(TAG, "onCreateView");
         View v = (View)inflater.inflate(R.layout.userinfo_layout, container, false);
         View.OnClickListener handler = new View.OnClickListener(){
            public void onClick(View v) {
               switch (v.getId()) {
                  case R.id.b_userinfo_register: 
                     NetworkUtils.register(Utils.serializeUserProfile(),
                           new BaseRequestListener() {
                              @Override
                        public void onComplete(final String response, Object state) {
                           getActivity().runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                 try {
                                    String errMsg = new JSONObject(response).getString("error");
                                    Utils.toastNotification(getActivity(), errMsg);
                                 } catch (JSONException e) {
                                    Utils.toastNotification(getActivity(), "Registered.");
                                 }
                              }
                           });
                        }
                     }
                     );
                     break;
                  case R.id.b_userinfo_update:
                     update();
                     break;
               }
            }
         };
         currentCode = getArguments() == null ?
            CODE_UPDATE : getArguments().getInt(TAG, CODE_UPDATE);
         switch (currentCode) {
            case CODE_REGISTER:
               v.findViewById(R.id.b_userinfo_register).setOnClickListener(handler);
               break;
            case CODE_UPDATE:
               v.findViewById(R.id.b_userinfo_update).setOnClickListener(handler);
               v.findViewById(R.id.b_userinfo_register).setVisibility(View.GONE);
               v.findViewById(R.id.l_userinfo_password).setVisibility(View.GONE);
               break;
         }

         // Populate from getUserProfile()
         Profile user = Utils.getUserProfile();
         if (user == null) {
            return v;
         }


         QuickContactBadge badge = (QuickContactBadge)v.findViewById(R.id.userinfo_badge); 
         Bitmap bitmap = Utils.getBitmap(user.getAvatar());
         if (bitmap != null) {
            badge.setImageBitmap(bitmap);
         }
         else {
            badge.setImageToDefault();
            badge.setTag(user.getAvatar());
            new NetworkUtils.DownloadImagesTask().execute(badge);
         }

         EditUsername = ((EditText)v.findViewById(R.id.e_userinfo_username));
         EditUsername.setText(user.getUsername());

         RadioGender = ((RadioGroup)v.findViewById(R.id.radioGender));
         RadioGender.check(user.getGender().trim().equalsIgnoreCase("Male") ?  R.id.radioMale : R.id.radioFemale);

         TextBirthdate = ((TextView)v.findViewById(R.id.e_userinfo_dob));
         TextBirthdate.setText(user.getBirthdate().toString());
         TextBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               final Date birthdate = Utils.getUserProfile().getBirthdate();
               final Calendar c = Calendar.getInstance();
               c.setTime(birthdate);
               new DatePickerDialog(getActivity(), 
                  new DatePickerDialog.OnDateSetListener() {
                     // when dialog box is closed, below method will be called.
                     public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        c.set(selectedYear, selectedMonth, selectedDay);
                        birthdate.setTime(c.getTimeInMillis());
                        TextBirthdate.setText(birthdate.toString());
                     }
                  }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
         });

         EditEmail = ((EditText)v.findViewById(R.id.e_userinfo_email));
         EditEmail.setText(user.getEmail());

         EditInterests = ((MultiAutoCompleteTextView)v.findViewById(R.id.e_userinfo_interests));
         EditInterests.setText(
               Utils.ListToString(
                  Utils.getUserProfile().getInterests(),
                  ", "));
         Set<String> interests = Utils.getInterests();
         EditInterests.setAdapter(
               new ArrayAdapter<String>(getActivity(),
                  android.R.layout.simple_dropdown_item_1line, 
                  interests.toArray(new String[interests.size()]))
               );
         EditInterests.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

         return v;
      }

   @Override
      public void onPause() {
         super.onPause();
         Log.d(TAG, "onPause");
         if (currentCode == CODE_REGISTER) {
            return;
         }
      }

   private void update() {
      Profile user = Utils.getUserProfile();
      user.setUsername(
            EditUsername.getText().toString()
            );
      user.setGender(
            Utils.genders[
            RadioGender.indexOfChild(RadioGender.findViewById(RadioGender.getCheckedRadioButtonId()))
            ]
            );
      user.setBirthdate(
            TextBirthdate.getText().toString()
            );
      user.setEmail(
            EditEmail.getText().toString()
            );
      List<String> interests = 
         new ArrayList<String>(Arrays.asList(EditInterests
                  .getText().toString().split(",")));
      List<String> userInterests = user.getInterests();
      userInterests.clear();
      for (String interest : interests) {
         String interest_trim = interest.trim();
         if (interest_trim.equals("")) {
            continue;
         }
         userInterests.add(interest_trim);
      }
      NetworkUtils.setProfile(
            Utils.serializeUserProfile(),
            new BaseRequestListener() {
               @Override
         public void onComplete(final String response, Object state) {
            getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  try {
                     String errMsg = new JSONObject(response).getString("error");
                     Utils.toastNotification(getActivity(), errMsg);
                  } catch (JSONException e) {
                     Utils.toastNotification(getActivity(), "User info is updated.");
                  }
               }
            });
         }
         });
   }

}
