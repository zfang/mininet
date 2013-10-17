package com.mininet;

import android.support.v4.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.mininet.AccountMgmtActivity;
import com.mininet.utils.Utils;
import com.mininet.utils.Utils.Item;

public class LoginActivity extends FragmentActivity {

   public static final String TAG = "LoginActivity";
   EditText
      Username,
      Password;
   Button LoginButton;
   /** Called when the activity is first created. */
   @Override
      public void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);

         if (AccountMgmtActivity.extend()) {
            proceed();
         }

         setContentView(R.layout.login_layout);
         View.OnClickListener handler = new View.OnClickListener(){
            public void onClick(View v) {

               switch (v.getId()) {
                  case R.id.blogin: 
                     authenticate(AccountMgmtActivity.CODE_MININET, 
                           Username.getText().toString(), 
                           Password.getText().toString());
                     break;
                  case R.id.bregister: 
                     getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, 
                              UserInfoFragment.newInstance(UserInfoFragment.CODE_REGISTER))
                        .commit();
                     break;
                  case R.id.bloginothers: 
                     new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Select an account")
                        .setAdapter(Item.getItemListAdapter(LoginActivity.this, 
                                 AccountMgmtActivity.accountItems), 
                              new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int item) {
                                    authenticate(item+1, "", "");
                                 }
                              }).show();
                     break;
               }
            }
         };
         LoginButton = (Button)findViewById(R.id.blogin);
         LoginButton.setOnClickListener(handler);
         findViewById(R.id.bregister).setOnClickListener(handler);
         findViewById(R.id.bloginothers).setOnClickListener(handler);

         Username = (EditText) findViewById(R.id.e_mininet_username);
         Password = (EditText) findViewById(R.id.e_mininet_password);
         TextWatcher watcher = new TextWatcher() {

            @Override
               public void afterTextChanged(Editable s) {
                  updateLoginButton();
               }
            @Override
               public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {
               }

            @Override
               public void onTextChanged(CharSequence s, int start, int before,
                     int count) {
               }
         };
         Username.addTextChangedListener(watcher);
         Password.addTextChangedListener(watcher);
         updateLoginButton();
      }
   private void updateLoginButton() {
      LoginButton.setEnabled(!Utils.isEmpty(Username) && !Utils.isEmpty(Password));
   }

   private void authenticate(int item, String username, String password) {
      startActivityForResult(new Intent(this, AccountMgmtActivity.class)
            .putExtra("username", username)
            .putExtra("password", password)
            , item);
   }

   @Override
      public void startActivityForResult(Intent intent, int requestCode) {
         intent.putExtra("requestCode", requestCode);
         super.startActivityForResult(intent, requestCode);
      }

   @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
            proceed();
         }
         else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            if (data == null) {
               return;
            }
            builder.setMessage("Login failed: " + 
                  data.getStringExtra(AccountMgmtActivity.FAIL_MSG))
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                  }
               }).show();
         }
      }

   private void proceed() {
      startActivity(new Intent(LoginActivity.this, MainActivity.class));
      finish();
   }
}
