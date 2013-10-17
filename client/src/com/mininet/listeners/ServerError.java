package com.mininet.listeners;

public class ServerError extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private int mErrorCode = 0;
   private String mErrorType;

   public ServerError(String message) {
      super(message);
   }

   public ServerError(String message, String type, int code) {
      super(message);
      mErrorType = type;
      mErrorCode = code;
   }

   public int getErrorCode() {
      return mErrorCode;
   }

   public String getErrorType() {
      return mErrorType;
   }

}


