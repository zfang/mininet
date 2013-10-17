package com.mininet.accountutils.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.mininet.AccountMgmtActivity;
import com.mininet.accountutils.AccountUtils;
import com.mininet.accountutils.SessionStore;
import com.mininet.client2server.NetworkUtils;
import com.mininet.datatypes.Profile;
import com.mininet.datatypes.SocialNetwork;
import com.mininet.listeners.RequestListener;
import com.mininet.listeners.ServerError;
import com.mininet.utils.Utils;


public class FacebookUtils implements AccountUtils {

   public static final String TAG = "FacebookUtils";
   private static final String KEY = "facebook-session";

   private static Facebook mFacebook = null;
   private static AsyncFacebookRunner mAsyncFacebookRunner = null;
   private static Session mFacebookSession = null;

   private static final String Facebook_APP_ID = "331703806915154";

   public static final String[] user_permissions = { "user_about_me", "user_activities", "user_birthday",
      "user_checkins", "user_education_history", "user_events", "user_groups",
      "user_hometown", "user_interests", "user_likes", "user_location", "user_notes",
      "user_online_presence", "user_photos", "user_photo_video_tags", "user_relationships",
      "user_relationship_details", "user_religion_politics", "user_status", "user_videos",
      "user_website", "user_work_history" };

   public static final String[] friend_permissions = { "friends_about_me", "friends_activities", "friends_birthday",
      "friends_checkins", "friends_education_history", "friends_events", "friends_groups",
      "friends_hometown", "friends_interests", "friends_likes", "friends_location",
      "friends_notes", "friends_online_presence", "friends_photos",
      "friends_photo_video_tags", "friends_relationships", "friends_relationship_details",
      "friends_religion_politics", "friends_status", "friends_videos", "friends_website",
      "friends_work_history" };

   public static final String[] extended_permissions = { "ads_management", "create_event", "create_note", "email",
      "export_stream", "manage_friendlists", "manage_pages",
      "offline_access", "publish_actions", "photo_upload", "publish_checkins",
      "publish_stream", "read_friendlists", "read_insights", "read_mailbox", "read_requests",
      "read_stream", "rsvp_event", "share_item", "status_update", "sms", "video_upload",
      "xmpp_login" };

   public static final String [] user_fields = {
      "id",
      "name",
      "first_name",
      "last_name",
      "gender",
      "locale",
      "language",
      "link",
      "username",
      "third_party_id",
      "installed",
      "timezone",
      "updated_time",
      "verified",
      "bio",
      "birthday",
      "cover",
      "currency",
      "devices",
      "education",
      "email",
      "hometown",
      "interested_in",
      "location",
      "political",
      "favorite_athletes",
      "favorite_team",
      "picture",
      "quotes",
      "relationship_status",
      "religion",
      "security_settings",
      "significant_other",
      "video_upload_limits",
      "website",
      "work",
   };

   private Handler mHandler;

   public FacebookUtils() {
      mHandler = new Handler();
   }

   public static Facebook getFacebook() {
      if (mFacebook == null) {
         mFacebook = new Facebook(Facebook_APP_ID);
      }
      return mFacebook;
   }

   public static AsyncFacebookRunner getAsyncFacebookRunner() {
      if (mAsyncFacebookRunner == null) {
         mAsyncFacebookRunner = new AsyncFacebookRunner(getFacebook());
      }
      return mAsyncFacebookRunner;
   }

   public static Session getFacebookSession() {
      if (mFacebookSession == null) {
         mFacebookSession = new Session(getFacebook());
      }
      return mFacebookSession;
   }

   private void registerCheck(final AccountMgmtActivity act) {
      // Get ID and request User Data
      // act.startProgressDialog();
      query(
            new HashSet<String>() {{
               add("id");
            }},
            new com.mininet.listeners.BaseRequestListener() {
               @Override
         public void onComplete(String response, Object state) {
            JSONObject jsonObject;
            final String id;
            try {
               jsonObject = new JSONObject(response);
               id = jsonObject.getString("id");
               NetworkUtils.login(
                  AccountMgmtActivity.CODE_FACEBOOK, 
                  id, 
                  new RequestListener(){

                     @Override
                  public void onComplete(String response,
                     Object state) {
                     if (response.equals("")) {
                        register(act);
                        return;
                     }
                     else {
                        Utils.setUserProfile(response);
                        if (Utils.getUserProfile() == null) {
                           act.onAuthFail("Server error");
                           return;
                        }
                        act.onAuthSucceed();
                     }
                  }

               @Override
                  public void onIOException(IOException e,
                        Object state) {
                     Log.e(TAG, e.getMessage());
                     act.onAuthFail(e.getMessage());
                  }

               @Override
                  public void onFileNotFoundException(
                        FileNotFoundException e, Object state) {
                     Log.e(TAG, e.getMessage());
                     act.onAuthFail(e.getMessage());
                        }

               @Override
                  public void onMalformedURLException(
                        MalformedURLException e, Object state) {
                     Log.e(TAG, e.getMessage());
                     act.onAuthFail(e.getMessage());
                        }

               @Override
                  public void onServerError(ServerError e,
                        Object state) {
                     Log.e(TAG, e.getMessage());
                     act.onAuthFail(e.getMessage());
                  }
                  });
               Log.d(TAG, jsonObject.toString());
            } catch (JSONException e) {
               Log.e(TAG, e.getMessage());
               act.onAuthFail(e.getMessage());
               return;
            }
         }
            }
      );
   }

   @Override
      public void login(final AccountMgmtActivity act) {
         Log.d(TAG, "login");
         if (SessionStore.restore(getFacebookSession(), act.getApplicationContext(), KEY)) {
            Log.v(TAG, "restore succeeded");
            registerCheck(act);
            return;
         }
         ArrayList<String> permissions = new ArrayList<String>(Arrays.asList(user_permissions));
         permissions.addAll(Arrays.asList(extended_permissions));
         getFacebook().authorize(
               act, 
               permissions.toArray(new String[permissions.size()]), 
               AccountMgmtActivity.CODE_FACEBOOK,
               new DialogListener() {
                  @Override
            public void onComplete(Bundle values) {
               Log.v(TAG, "authorize onComplete");
               SessionStore.save(getFacebookSession(), act.getApplicationContext(), KEY);
               registerCheck(act);
            }
         @Override
            public void onFacebookError(FacebookError error) {
               Log.e(TAG, error.getMessage());
               act.onAuthFail(error.getMessage());
            }

         @Override
            public void onError(DialogError error) {
               Log.e(TAG, error.getMessage());
               act.onAuthFail(error.getMessage());
            }

         @Override
            public void onCancel() {
               String msg = "Action Cancelled";
               Log.e(TAG, msg);
               act.onAuthFail(msg);
            }
            });
      }

   @Override
      public void loginCallback(int requestCode, int resultCode, Intent data) {
         getFacebook().authorizeCallback(requestCode, resultCode, data);
      }

   @Override
      public void logout(final Context context) {
         SessionStore.clear(context, KEY);
         Log.d(TAG, "logged out");
      }

   @Override
      public boolean extend(final Context context) {
         if (!SessionStore.restore(getFacebookSession(), context, KEY)) {
            return false;
         }
         boolean extended = getFacebook().extendAccessTokenIfNeeded(context, null);
         if (extended) {
            SessionStore.save(getFacebookSession(), context, KEY);
            return true;
         }
         return false;
      }

   @Override
      public void getAllData(final RequestListener rl) {
         // TODO Auto-generated method stub
      }

   @Override
      public void query(Set<String> set, final RequestListener listener) {
         final Bundle params = new Bundle();
         StringBuffer sb = new StringBuffer();
         for (String str : set) {
            if (sb.length() > 0) {
               sb.append(", ");
            }
            sb.append(str);
         }
         params.putString("fields", sb.toString());
         getAsyncFacebookRunner().request("me", params, new BaseRequestListener() {
            @Override
            public void onComplete(String response, Object state) {
               listener.onComplete(response, state);
            }
         });
      }

   public void queryPicture(final RequestListener listener) {
      final Bundle params = new Bundle();
      params.putString("width", "100");
      params.putString("height", "100");
      params.putString("redirect", "false");
      getAsyncFacebookRunner().request("me/picture", 
            params, new BaseRequestListener() {
         @Override
         public void onComplete(String response, Object state) {
            listener.onComplete(response, state);
         }
      });
   }

   // Session Wrapper
   private static class Session implements com.mininet.accountutils.Session {
      private Facebook f_;
      public Session(Facebook f) {
         f_ = f;
      }
      @Override
         public boolean isSessionValid() {
            return f_.isSessionValid();
         }
      @Override
         public String getAccessToken() {
            return f_.getAccessToken();
         }
      @Override
         public long getAccessExpires() {
            return f_.getAccessExpires();
         }
      @Override
         public void setAccessToken(String str) {
            f_.setAccessToken(str);
         }
      @Override
         public void setAccessExpires(long l) {
            f_.setAccessExpires(l);
         }
   }

   private Profile JSONObjectToProfile(JSONObject jsonObject) {
      if (jsonObject == null) {
         return null;
      }

      Profile profile = new Profile();
      // Social Network
      try {
         profile.getSocialnetworks().add(new SocialNetwork("facebook", jsonObject.getString("id")));
      } catch (JSONException e) { }
      // ID
      try {
         profile.setId(jsonObject.getString("id"));
      } catch (JSONException e) { }
      // User Name
      try {
         profile.setUsername(jsonObject.getString("name"));
      } catch (JSONException e) { }
      try {
      // AvatarUrl 
         profile.setAvatar(
               jsonObject.getJSONObject("picture").getJSONObject("data").getString("url"));
      } catch (JSONException e) { }
      // Gender
      try {
         profile.setGender(Utils.capitalize(jsonObject.getString("gender")));
      } catch (JSONException e) { }
      // Email 
      try {
         profile.setEmail(jsonObject.getString("email"));
      } catch (JSONException e) { }
      // BirthDate 
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
         Date birthday = null;
         birthday = sdf.parse(jsonObject.getString("birthday"));
         sdf.applyPattern("yyyy-MM-dd");
         profile.setBirthdate(sdf.format(birthday));
      } catch (ParseException e) {
      } catch (JSONException e) { }

      // Interests 
      try {
         List<String> interests = profile.getInterests();
         JSONArray interests_array = jsonObject.getJSONObject("interests").getJSONArray("data");
         for (int i = interests_array.length() - 1; i >= 0 ; --i) {
            interests.add(interests_array.getJSONObject(i).getString("name").toLowerCase());
         }
      } catch (JSONException e) { }

      Log.d(TAG, Utils.serializeProfile(profile));
      return profile;
   }

   @Override
      public void register(final AccountMgmtActivity act) {
         query(
               new HashSet<String>() {{
                  add("id");
                  add("name");
                  add("gender");
                  add("birthday");
                  add("email");
                  add("education");
                  add("work");
                  add("bio");
                  add("interests");
                  add("favorite_athletes");
               }},
               new com.mininet.listeners.BaseRequestListener() {
                  @Override
            public void onComplete(final String profileResponse, Object state) {
               queryPicture(
                  new com.mininet.listeners.BaseRequestListener() {
                     @Override
                  public void onComplete(final String pictureResponse, Object state) {
                     setProfile(act, profileResponse, pictureResponse);
                  }
                  }
                  );
            }
               }
         );
      }

   private void setProfile(final AccountMgmtActivity act, 
         final String profileResponse, final String pictureResponse) {
      try {
         JSONObject jsonObject;
         jsonObject = new JSONObject(profileResponse);
         jsonObject.put("picture", new JSONObject(pictureResponse));
         Utils.setUserProfile(FacebookUtils.this.JSONObjectToProfile(jsonObject));
         NetworkUtils.register(
               Utils.serializeUserProfile(),
               new RequestListener(){

                  @Override
            public void onComplete(String response, Object state) {
               try {
                  String errMsg = new JSONObject(response).getString("error");
                  act.onAuthFail(errMsg);
               } catch (JSONException e) {
                  act.onAuthSucceed();
               }
            }

         @Override
            public void onIOException(IOException e, Object state) {
               Log.e(TAG, e.getMessage());
               act.onAuthFail(e.getMessage());
            }

         @Override
            public void onFileNotFoundException(FileNotFoundException e, Object state) {
               Log.e(TAG, e.getMessage());
               act.onAuthFail(e.getMessage());
            }

         @Override
            public void onMalformedURLException(MalformedURLException e, Object state) {
               Log.e(TAG, e.getMessage());
               act.onAuthFail(e.getMessage());
            }

         @Override
            public void onServerError(ServerError e, Object state) {
               Log.e(TAG, e.getMessage());
               act.onAuthFail(e.getMessage());
            }
            });
      } catch (JSONException e) { 
         Log.e(TAG, e.getMessage());
         act.onAuthFail(e.getMessage());
      }
   }

}
