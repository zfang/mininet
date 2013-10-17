package com.mininet;

import java.sql.Date;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.utils.Utils;

public class ProfileActivity extends FragmentActivity {
   public static final String TAG = "ProfileActivity";

   public static final int CODE_ADD = 0;
   public static final int CODE_DELETE = 1;
   public static final int CODE_BLOCK = 2;
   public static final int CODE_UNBLOCK = 3;
   public static final int CODE_LOCATE = 4;

   Button 
      AddContact,
      DeleteContact,
      UnblockContact,
      BlockContact,
      LocateContact;

   @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.profiles_layout);
         AddContact = (Button) findViewById(R.id.b_add_contact);
         DeleteContact = (Button) findViewById(R.id.b_delete_contact);
         UnblockContact = (Button) findViewById(R.id.b_unblock_contact);
         BlockContact = (Button) findViewById(R.id.b_block_contact);
         LocateContact = (Button) findViewById(R.id.b_locate_contact);

         final Intent in = getIntent();
         String profileType = in.getStringExtra(TAG);

         if (profileType.equals(BlockedContactsFragment.TAG)) {
               AddContact.setVisibility(View.GONE);
               DeleteContact.setVisibility(View.GONE);
               BlockContact.setVisibility(View.GONE);
               LocateContact.setVisibility(View.GONE);
               UnblockContact.setOnClickListener(new View.OnClickListener(){
                  public void onClick(View v) {
                     in.putExtra(TAG, CODE_UNBLOCK);
                     setResult(RESULT_OK, in);
                     finish();
                  }
               });
               populate(true);
         }
         else {
            if (profileType.equals(SearchFragment.TAG)) {
               DeleteContact.setVisibility(View.GONE);
               BlockContact.setVisibility(View.GONE);
               if (Utils.getContactProfiles(false).get(
                        Utils.getCurrentContactProfile().getId()) != null) {
                  AddContact.setVisibility(View.GONE);
                        }
               else {
                  LocateContact.setVisibility(View.GONE);
                  AddContact.setOnClickListener(new View.OnClickListener(){
                     public void onClick(View v) {
                        in.putExtra(TAG, CODE_ADD);
                        setResult(RESULT_OK, in);
                        finish();
                     }
                  });
               }
            }
            else if (profileType.equals(ContactsFragment.TAG)) { 
               AddContact.setVisibility(View.GONE);
               DeleteContact.setOnClickListener(new View.OnClickListener(){
                  public void onClick(View v) {
                     in.putExtra(TAG, CODE_DELETE);
                     setResult(RESULT_OK, in);
                     finish();
                  }
               });
               BlockContact.setOnClickListener(new View.OnClickListener(){
                  public void onClick(View v) {
                     in.putExtra(TAG, CODE_BLOCK);
                     setResult(RESULT_OK, in);
                     finish();
                  }
               });
            }

            UnblockContact.setVisibility(View.GONE);
            LocateContact.setOnClickListener(new View.OnClickListener(){
               public void onClick(View v) {
                  startActivity(new Intent(ProfileActivity.this, MapActivity.class));
               }
            });
            populate(false);
         }

      }

   @SuppressLint("NewApi")
      private void populate(boolean blocked) {
         Profile contact = Utils.getCurrentContactProfile();
         ((TextView)findViewById(R.id.profile_username))
            .setText(contact.getUsername());
         ((TextView)findViewById(R.id.profile_gender))
            .setText(contact.getGender());

         Date birthdate = Utils.getCurrentContactProfile().getBirthdate();
         ((TextView)findViewById(R.id.profile_dob))
            .setText(birthdate == null ? "" : birthdate.toString());

         // Interests
         ((TextView)findViewById(R.id.profile_interests))
            .setText(
                  contact.getInterests() == null ?
                  "" : Utils.ListToString(
                     contact.getInterests(),
                     ", "));

         QuickContactBadge badge = (QuickContactBadge)findViewById(R.id.profile_badge);
         Bitmap bitmap = Utils.getBitmap(contact.getAvatar());
         if (bitmap != null) {
            badge.setImageBitmap(bitmap);
         }
         else if (contact.getAvatar() == null) {
            badge.setImageToDefault();
         }
         else {
            badge.setImageToDefault();
            badge.setTag(contact.getAvatar());
            new NetworkUtils.DownloadImagesTask().execute(badge);
         }

         badge.setOnClickListener(null);

         if (!blocked) {
            badge.assignContactFromEmail(Utils.getCurrentContactProfile().getEmail(), true);
         }
         else {
            badge.setClickable(false);
            badge.setOnClickListener(null);
            findViewById(R.id.profile_basic_info).setVisibility(View.GONE);
         }

         if (contact.getId().equals(Utils.getUserProfile().getId())) {
            AddContact.setVisibility(View.GONE);
            DeleteContact.setVisibility(View.GONE);
            BlockContact.setVisibility(View.GONE);
            LocateContact.setVisibility(View.GONE);
            UnblockContact.setVisibility(View.GONE);
         }

      }

   @Override
      public void onBackPressed() {
         Utils.setCurrentContactProfile(null);
         super.onBackPressed();
      }
}

