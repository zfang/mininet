package com.mininet.accountutils.google;

import java.util.Set;

import android.accounts.Account;
import android.accounts.AccountManager;

import android.util.Log;

import android.content.Intent;
import android.content.Context;

import com.mininet.AccountMgmtActivity;
import com.mininet.accountutils.AccountUtils;
import com.mininet.listeners.RequestListener;

public class GoogleUtils implements AccountUtils {

   public static final String TAG = "GoogleUtils";

   private static final String KEY = "google-session";
   private static final String AUTH_TYPE_GOOGLE = "com.google";

	@Override
      public void login(final AccountMgmtActivity act) {
         Account [] accts = AccountManager.get(act).getAccountsByType(AUTH_TYPE_GOOGLE);
         if (accts != null) {
            for (Account acct : accts) {
               Log.d(TAG, acct.name);
               break;
            }
         }
         act.onAuthFail("Not implemented");
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

