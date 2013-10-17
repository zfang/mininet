package com.mininet.accountutils;

import java.util.Set;

import android.content.Intent;
import android.content.Context;

import com.mininet.AccountMgmtActivity;

import com.mininet.listeners.RequestListener;

public interface AccountUtils {
   public void register(final AccountMgmtActivity act);
   public void login(final AccountMgmtActivity act);
   public void loginCallback(int requestCode, int resultCode, Intent data);
   public void logout(final Context context);
   public boolean extend(final Context context);
   public void getAllData(final RequestListener rl);
   public void query(Set<String> set, final RequestListener rl);
}

