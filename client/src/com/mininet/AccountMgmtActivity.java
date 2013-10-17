package com.mininet;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.mininet.accountutils.AccountUtils;
import com.mininet.accountutils.SessionEvents;
import com.mininet.accountutils.facebook.FacebookUtils;
import com.mininet.accountutils.google.GoogleUtils;
import com.mininet.accountutils.linkedin.LinkedInUtils;
import com.mininet.accountutils.mininet.MininetUtils;
import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.datatypes.SocialNetwork;
import com.mininet.listeners.BaseRequestListener;
import com.mininet.utils.ProgressDialogActivity;
import com.mininet.utils.Utils;
import com.mininet.utils.Utils.Item;

public class AccountMgmtActivity extends ProgressDialogActivity implements SessionEvents.AuthListener {
   
   // TODO: Should try android.accounts.AccountManager

   // static functions are those that don't return in a callback fashion
   // non-static functions are those that need to return in a callback fashion
   
   public static final String TAG = "AuthAcctManager";
   public static final String FAIL_MSG = "AuthAcctManager.FAIL_MSG";

   public static final Item [] accountItems = {
      new Item("Facebook", R.drawable.ic_dialog_facebook)
      //, new Item("Google", R.drawable.ic_dialog_google)
      //, new Item("LinkedIn", R.drawable.ic_dialog_linkedin)
   };

   public static final String [] networks = {
      "mininet",
      "facebook",
      "google",
      "linkedin",
   };

   public static final int CODE_MININET = 0;
   public static final int CODE_FACEBOOK = 1;
   public static final int CODE_GOOGLE = 2;
   public static final int CODE_LINKEDIN = 3;
   public static final int CODE_LOGOUT = 4;

   private static final int ACCT_MIN = -1;
   private static final int ACCT_MAX = CODE_LOGOUT;

   private static AccountUtils [] acctUtils = new AccountUtils[ACCT_MAX];

   private static boolean verifyIndex(int id) {
      return !(id <= ACCT_MIN || id >= ACCT_MAX); 
   }

   private int currentNetwork;

   public static AccountUtils getAccount() {
      for (AccountUtils au : acctUtils) {
         if (au == null) {
            continue;
         }
         return au;
      }
      return null;
   }

   public static AccountUtils getAccount(int id) {
      if (!verifyIndex(id)) {
         return null;
      }
      return acctUtils[id];
   }

   public static boolean extend() {
      Log.d(TAG, "extend");
      boolean extended = false;
      for (AccountUtils au : acctUtils) {
         if (au == null || !au.extend(Utils.getAppContext())) {
            continue;
         }
         extended = true;
      }
      return extended;
   }

   public void login(int taskId, String username, String password) {
      Log.d(TAG, "login");
      if (!verifyIndex(taskId)) {
         return;
      }
      if (acctUtils[taskId] == null) {
         switch (taskId) {
            case CODE_MININET:
               acctUtils[taskId] = new MininetUtils(username, password);
               break;
            case CODE_FACEBOOK:
               acctUtils[taskId] = new FacebookUtils();
               break;
            case CODE_GOOGLE:
               acctUtils[taskId] = new GoogleUtils();
               break;
            case CODE_LINKEDIN:
               acctUtils[taskId] = new LinkedInUtils();
               break;
         }
      }
      new LoginTask().execute(taskId);
   }

   public void logout() {
      new LogoutTask().execute();
   }

   @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
            case CODE_MININET:
            case CODE_FACEBOOK:
            case CODE_GOOGLE:
            case CODE_LINKEDIN:
               acctUtils[requestCode].loginCallback(requestCode, resultCode, data);
               break;
         }
      }

   @Override
      public void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         Intent in = getIntent();
         int taskId = in.getIntExtra("requestCode", -1);
         currentNetwork = taskId;
         switch (taskId) {
            case CODE_MININET:
            case CODE_FACEBOOK:
            case CODE_GOOGLE:
            case CODE_LINKEDIN:
               login(taskId, in.getStringExtra("username"), in.getStringExtra("password"));
               break;
            case CODE_LOGOUT:
               logout();
               break;
         }
      }

   private void getPrivacy() {
      Profile user = Utils.getUserProfile();
      BaseRequestListener listener =  new BaseRequestListener() {
         @Override
            public void onComplete(String response, Object state) {
               Utils.setUserPrivacy(response);
            }};
      switch (currentNetwork) {
         case CODE_MININET:
            NetworkUtils.login(
                  user.getId(),
                  user.getPassword(),
                  listener,
                  true
                  );
            break;
         case CODE_FACEBOOK:
         case CODE_GOOGLE:
         case CODE_LINKEDIN:
            Map<String, String> socialnetworks = new HashMap<String, String>();
            for (SocialNetwork network : user.getSocialnetworks()){
               socialnetworks.put(network.getName(), network.getId());
            }
            NetworkUtils.login(
                  currentNetwork,
                  socialnetworks.get(networks[currentNetwork]),
                  listener,
                  true
                  );
            break;
      }
   }

   @Override
      public void onAuthSucceed() {
         Log.i(TAG, "Auth success");
         getPrivacy();
         stopProgressDialog();
         setResult(RESULT_OK, getIntent());
         finish();
      }

   @Override
      public void onAuthFail(String error) {
         Log.w(TAG, error);
         stopProgressDialog();
         Intent in = getIntent();
         in.putExtra(FAIL_MSG, error);
         setResult(~RESULT_OK, in);
         finish();

      }

   private class LoginTask extends AsyncTask<Integer, Void, Void> {

      @Override
         public void onPreExecute() {
            startProgressDialog();
         }

      @Override
         public Void doInBackground(Integer... args) {
            Log.d(TAG, "login id: " + args[0]);
            acctUtils[args[0]].login(AccountMgmtActivity.this);
            return null;
         }

      @Override
         public void onPostExecute(Void v) {
         }
   }

   private class LogoutTask extends AsyncTask<Void, Void, Void> {

      @Override
         public void onPreExecute() {
            startProgressDialog();
         }

      @Override
         public Void doInBackground(Void... args) {
            Log.d(TAG, "logout");
            for (AccountUtils au : acctUtils) {
               if (au == null) {
                  continue;
               }
               au.logout(Utils.getAppContext());
               au = null;
            }
            return null;
         }

      @Override
         public void onPostExecute(Void v) {
            stopProgressDialog();
            setResult(RESULT_OK, getIntent());
            finish();
         }
   }
}
