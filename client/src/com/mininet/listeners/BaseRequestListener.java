package com.mininet.listeners;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.MalformedURLException;

import android.util.Log;

public abstract class BaseRequestListener implements RequestListener {

   public static final String TAG = "BaseRequestListener";

    @Override
    public void onFileNotFoundException(FileNotFoundException e, final Object state) {
        Log.e("TAG", e.getMessage());
    }

    @Override
    public void onIOException(IOException e, final Object state) {
        Log.e("TAG", e.getMessage());
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, final Object state) {
        Log.e("TAG", e.getMessage());
    }

    @Override
    public void onServerError(ServerError e, final Object state) {
        Log.e("TAG", e.getMessage());
    }

}


