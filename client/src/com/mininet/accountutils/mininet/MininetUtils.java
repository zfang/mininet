package com.mininet.accountutils.mininet;

import java.util.Map;
import java.util.Set;

import android.os.Handler;

import android.content.Context;
import android.content.Intent;

import com.mininet.client2server.NetworkUtils;

import com.mininet.AccountMgmtActivity;
import com.mininet.accountutils.AccountUtils;
import com.mininet.listeners.RequestListener;

import com.mininet.listeners.BaseRequestListener;

public class MininetUtils implements AccountUtils {

   public static final String AUTH_TYPE_MININET = "com.mininet";
   private static final String KEY = "mininet-session";

   private String username, password;

   private Handler mHandler;

   public MininetUtils(String u, String p) {
      username = u;
      password = p;
      mHandler = new Handler();
   }

	@Override
      public void login(final AccountMgmtActivity act) {
         mHandler.post(new Runnable() {
            @Override
            public void run() {
               NetworkUtils.login(username, password, new BaseRequestListener(){
                  @Override
                  public void onComplete(String response, Object state) {
                     // TODO
                  }
               });
            }
         });
      }

   @Override
      public void loginCallback(int requestCode, int resultCode, Intent data) {
         // TODO Auto-generated method stub

      }

   @Override
      public void logout(final Context context) {
         // TODO Auto-generated method stub

      }

   @Override
      public boolean extend(final Context context) {
         // TODO Auto-generated method stub
         return false;
      }

   @Override
      public void getAllData(final RequestListener rl) {
         // TODO Auto-generated method stub
      }

   @Override
      public void query(Set<String> set, final RequestListener rl) {
         // TODO Auto-generated method stub
      }

@Override
public void register(AccountMgmtActivity act) {
	// TODO Auto-generated method stub
	
}

}


