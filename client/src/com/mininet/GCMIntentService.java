package com.mininet;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.mininet.client2server.NetworkUtils;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.Utils;

public class GCMIntentService extends GCMBaseIntentService {
   public static final String TAG = "GCMIntentService";

   public GCMIntentService() {
      super(Utils.GCM_SENDER_ID);
   }

   @Override
      protected void onError(Context arg0, String arg1) {
         Log.d(TAG, "onError: " + arg1);
      }

   @Override
      protected void onMessage(Context arg0, Intent arg1) {
         Utils.processMessage(arg1.getExtras().getString("message").toString());
      }

   @Override
      protected void onRegistered(Context arg0, String arg1) {
         final Context context = arg0;
         final String regId = arg1;
         NetworkUtils.registerGCM(
               Utils.getUserProfile().getId(),
               regId,
               new BaseRequestListener() {
                  @Override
            public void onComplete(String response, Object state) {
               GCMRegistrar.setRegisteredOnServer(context, true);
            }
            });
      }

   @Override
      protected void onUnregistered(Context arg0, String arg1) {
         Log.d(TAG, "onUnregistered: " + arg1);
      }
}
