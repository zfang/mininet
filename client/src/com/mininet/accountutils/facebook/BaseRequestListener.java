package com.mininet.accountutils.facebook;

import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.util.Log;

public abstract class BaseRequestListener implements RequestListener {

    @Override
    public void onFacebookError(FacebookError e, final Object state) {
        Log.e("Facebook", e.getMessage());
    }

    @Override
    public void onFileNotFoundException(FileNotFoundException e, final Object state) {
        Log.e("Facebook", e.getMessage());
    }

    @Override
    public void onIOException(IOException e, final Object state) {
        Log.e("Facebook", e.getMessage());
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, final Object state) {
        Log.e("Facebook", e.getMessage());
    }

}

