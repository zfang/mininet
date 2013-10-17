package com.mininet.client2server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.mininet.AccountMgmtActivity;
import com.mininet.listeners.RequestListener;
import com.mininet.utils.Utils;

public class NetworkUtils {
   public static final String TAG = "NetworkUtils";

   private static final String BASE_URL = "http://www.mininet.theodorepan.com";
   private static final String PROFILE_URL = BASE_URL + "/profile.php";
   private static final String LOGIN_URL = BASE_URL + "/login.php";
   private static final String SEARCH_URL = BASE_URL + "/search.php";
   private static final String REGISTER_URL = BASE_URL + "/register.php";
   private static final String CONTACT_URL = BASE_URL + "/contact.php";
   private static final String LOCATION_URL = BASE_URL + "/location.php";

   public static final String PARAM_USERNAME = "username";
   public static final String PARAM_PASSWORD = "password";
   public static final String PARAM_ID = "id";
   public static final String PARAM_NETWORK = "network";
   public static final String PARAM_QUERY = "q";
   public static final String PARAM_BLOCKED = "blocked";
   public static final String PARAM_ADD = "add";
   public static final String PARAM_DELETE = "delete";
   public static final String PARAM_BLOCK = "block";
   public static final String PARAM_UNBLOCK = "unblock";
   public static final String PARAM_PROFILE = "profile";
   public static final String PARAM_PRIVACY = "privacy";
   public static final String PARAM_LOCATION = "location";
   public static final String PARAM_GCM = "gcm";

   private static HttpClient mHttpClient = null;

   public synchronized static HttpClient getHttpClient() {
      if (mHttpClient != null) {
         return mHttpClient;
      }
      mHttpClient = new DefaultHttpClient();
      final HttpParams params = mHttpClient.getParams();
      mHttpClient = new DefaultHttpClient(
            new ThreadSafeClientConnManager(
               params, 
               mHttpClient.getConnectionManager().getSchemeRegistry()
               ),
            params
            );
      return mHttpClient;
   }

   public static class HttpPostTask extends AsyncTask<Object, Void, Void> {
      @Override
         public Void doInBackground(Object... objs) {
            final String url = (String)objs[0];
            @SuppressWarnings("unchecked")
               final List<NameValuePair> params = (List<NameValuePair>)objs[1];
            final RequestListener listener = (RequestListener)objs[2];

            final HttpEntity entity;
            try {
               entity = new UrlEncodedFormEntity(params);
               Log.d(TAG, EntityUtils.toString(entity));
            } catch (final UnsupportedEncodingException e) {
               Log.e(TAG, e.getMessage());
               return null;
            } catch (final IOException e) {
               Log.e(TAG, "IOException", e);
               listener.onIOException(e, null);
               return null;
            }
            HttpPost request = new HttpPost(url);
            request.setEntity(entity);
            try {
               listener.onComplete(
                     getHttpClient().execute(
                        request, 
                        new BasicResponseHandler()), 
                     null);
            } catch (final HttpResponseException e) {
               Log.e(TAG, "HttpResponseException", e);
            } catch (final IOException e) {
               Log.e(TAG, "IOException", e);
               listener.onIOException(e, null);
            } catch (final Exception e){
               Log.e(TAG, "Exception", e);
            }

            return null;
         }
   }

   public static class HttpGetTask extends AsyncTask<Object, Void, Void> {
      @Override
         public Void doInBackground(Object... objs) {
            String url = (String)objs[0];
            @SuppressWarnings("unchecked")
               final List<NameValuePair> params = (List<NameValuePair>)objs[1];
            final RequestListener listener = (RequestListener)objs[2];

            try {
               if(!url.endsWith("?")) {
                  url += "?";
               }
               url += URLEncodedUtils.format(params, "utf-8");
               Log.d(TAG, url);
               listener.onComplete(
                     getHttpClient().execute(
                        new HttpGet(url), 
                        new BasicResponseHandler()),
                     null);
            } catch (final HttpResponseException e) {
               Log.e(TAG, "HttpResponseException", e);
            } catch (final IOException e) {
               Log.e(TAG, "IOException", e);
            } catch (final Exception e){
               Log.e(TAG, "Exception", e);
            }
            return null;
         }
   }

   public static class DownloadImagesTask extends AsyncTask<Object, Void, Bitmap> {

      ImageView imageView = null;
      String url = null;
      RequestListener listener = null;

      @Override
         protected Bitmap doInBackground(Object... objs) {
            try{
               this.imageView = (ImageView)objs[0];
               if (imageView == null) {
                  this.url = (String)objs[1];
                  this.listener = (RequestListener)objs[2];
               }
               else {
                  this.url = (String)imageView.getTag();
               }
            } catch (Exception e){
               Log.e(TAG, "DownloadImagesTask: doInBackground " + e.getMessage());
            }
            return download_Image(url);
         }

      @Override
         protected void onPostExecute(Bitmap result) {
            try{
               if (imageView == null) {
                  if (listener != null) {
                     listener.onComplete(url, null);
                  }
               }
               else {
                  Drawable drawable = imageView.getDrawable();
                  if (drawable != null) {
                     Rect bounds = drawable.getBounds();
                     result = Bitmap.createScaledBitmap(
                           result, bounds.width(), bounds.height(), true);
                  }
                  else {
                     result = Bitmap.createScaledBitmap(
                           result, 100, 100, true);
                  }
                  imageView.setImageBitmap(result);
               }
               Utils.addBitmap(url, result);
            } catch (Exception e){
               Log.e(TAG, "DownloadImagesTask: onPostExecute " + e.getMessage());
            }
         }


      private Bitmap download_Image(String url) {
         Log.d(TAG, "DownloadImagesTask: " + url);
         Bitmap bitmap = null;
         try{
            bitmap = BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
         } catch (Exception e){
            Log.e(TAG, "DownloadImagesTask: download_Image " + e.getMessage());
         }
         return bitmap;
      }
   }
   /* 
    * The following methods receive JSONArray-parsable String 
    * from the server
    * 
    * */

   // mininet authenticate
   public static void login(final String id, final String password, RequestListener listener) {
      login(id, password, listener, false);
   }

   public static void login(final int code, final String id, RequestListener listener) {
      login(code, id, listener, false);
   }

   public static void login(final String id, final String password, RequestListener listener, boolean privacy) {
      final List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, id));
      params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
      if (privacy) {
         params.add(new BasicNameValuePair("privacy", "1"));
      }
      new HttpPostTask().execute(LOGIN_URL, params, listener);
   }

   public static void login(final int code, final String id, RequestListener listener, boolean privacy) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, id));
      switch (code) {
         case AccountMgmtActivity.CODE_FACEBOOK:
         case AccountMgmtActivity.CODE_GOOGLE:
         case AccountMgmtActivity.CODE_LINKEDIN:
            params.add(new BasicNameValuePair(PARAM_NETWORK, AccountMgmtActivity.networks[code]));
            if (privacy) {
               params.add(new BasicNameValuePair("privacy", "1"));
            }
            new HttpPostTask().execute(LOGIN_URL, params, listener);
      }
   }

   public static void search(String mininetId, Map<String, String> queries, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      StringBuffer sb = new StringBuffer();
      for (Map.Entry<String, String> entry : queries.entrySet()) {
         if (sb.length() > 0) {
            sb.append(",");
         }
         sb.append(entry.getKey()).append(":").append(entry.getValue());
      }
      Log.d(TAG, "query: " + sb.toString());
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_QUERY, sb.toString()));
      new HttpGetTask().execute(SEARCH_URL, params, listener);
   }

   public static void getProfile(final String mininetId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      new HttpGetTask().execute(PROFILE_URL, params, listener);
   }

   public static void getFriends(final String mininetId, RequestListener listener) {
      getContacts(mininetId, listener, "false");
   }

   public static void getBlocked(final String mininetId, RequestListener listener) {
      getContacts(mininetId, listener, "true");
   }

   public static void getContacts(final String mininetId, RequestListener listener, String blocked) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_BLOCKED, blocked));
      new HttpGetTask().execute(CONTACT_URL, params, listener);
   }

   public static void addContacts(final String mininetId, final String contactId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_ADD, contactId));
      new HttpPostTask().execute(CONTACT_URL, params, listener);
   }

   public static void deleteContacts(final String mininetId, final String contactId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_DELETE, contactId));
      new HttpPostTask().execute(CONTACT_URL, params, listener);
   }

   public static void blockContacts(final String mininetId, final String contactId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_BLOCK, contactId));
      new HttpPostTask().execute(CONTACT_URL, params, listener);
   }

   public static void unblockContacts(final String mininetId, final String contactId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_UNBLOCK, contactId));
      new HttpPostTask().execute(CONTACT_URL, params, listener);
   }

   public static void register(final String profileJsonString, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_PROFILE, profileJsonString));
      new HttpPostTask().execute(REGISTER_URL, params, listener);
   }

   public static void setProfile(final String profileJsonString, RequestListener listener) {
      Log.d(TAG, profileJsonString);
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_PROFILE, profileJsonString));
      new HttpPostTask().execute(PROFILE_URL, params, listener);
   }

   public static void setPrivacy(final String privacyJsonString, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_PRIVACY, privacyJsonString));
      new HttpPostTask().execute(PROFILE_URL, params, listener);
   }

   public static void registerGCM(final String mininetId, final String gcmId, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_ID, mininetId));
      params.add(new BasicNameValuePair(PARAM_GCM, gcmId));
      new HttpPostTask().execute(REGISTER_URL, params, listener);
   }

   public static void setLocation(final String locationJsonString, RequestListener listener) {
      List<NameValuePair> params = new LinkedList<NameValuePair>();
      params.add(new BasicNameValuePair(PARAM_LOCATION, locationJsonString));
      new HttpPostTask().execute(LOCATION_URL, params, listener);
   }

}

