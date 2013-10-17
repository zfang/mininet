package com.mininet.accountutils;

public interface Session {
   public boolean isSessionValid();
   public String getAccessToken();
   public long getAccessExpires();
   public void setAccessToken(String str);
   public void setAccessExpires(long l);
}

