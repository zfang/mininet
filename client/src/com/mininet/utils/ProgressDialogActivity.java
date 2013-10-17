package com.mininet.utils;

import android.support.v4.app.FragmentActivity;
import android.app.ProgressDialog;

public class ProgressDialogActivity extends FragmentActivity {
   private ProgressDialog pd = null;

   public void startProgressDialog() {
      stopProgressDialog();
      pd = ProgressDialog.show(
            this, 
            "", 
            "In progress. Please wait...", 
            true);
   }

   public void stopProgressDialog() {
      if (pd != null) {
         pd.dismiss();
         pd = null;
      }
   }

   @Override
      protected void onPause() {
         super.onPause();
         stopProgressDialog();
      }

   @Override
      protected void onStop() {
         super.onStop();
         stopProgressDialog();
      }


   @Override
      protected void onDestroy() {
         super.onDestroy();
         stopProgressDialog();
      }


}

