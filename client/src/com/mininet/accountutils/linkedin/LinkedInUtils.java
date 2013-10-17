package com.mininet.accountutils.linkedin;

import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.content.Context;

import com.mininet.AccountMgmtActivity;
import com.mininet.accountutils.AccountUtils;
import com.mininet.listeners.RequestListener;

public class LinkedInUtils implements AccountUtils {

   public static final String TAG = "LinkedInUtils";
   private static final String KEY = "linkedin-session";

	@Override
      public void login(final AccountMgmtActivity act) {
         // TODO Auto-generated method stub
         act.onAuthFail("Not implemented");
      }

   @Override
      public void logout(final Context context) {
         // TODO Auto-generated method stub

      }

   @Override
      public void loginCallback(int requestCode, int resultCode, Intent data) {
         // TODO Auto-generated method stub

      }

   @Override
      public boolean extend(final Context context) {
         // TODO Auto-generated method stub
         return false;
      }

   @Override
      public void getAllData(RequestListener rl) {
         // TODO Auto-generated method stub
      }

   @Override
      public void query(Set<String> set, RequestListener rl) {
         // TODO Auto-generated method stub
      }

@Override
public void register(AccountMgmtActivity act) {
	// TODO Auto-generated method stub
	
}

}

