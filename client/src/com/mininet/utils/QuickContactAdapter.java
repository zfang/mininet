package com.mininet.utils;  

import java.util.List;  
import java.util.Map;  
import android.annotation.SuppressLint;
import android.content.Context;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.QuickContactBadge;  
import android.widget.TextView;  

import android.view.inputmethod.EditorInfo;

import com.mininet.datatypes.Profile;  

import com.mininet.client2server.NetworkUtils;
import android.graphics.Bitmap;

public class QuickContactAdapter extends BaseAdapter {  
   public static final int CODE_TEXT_ID = 0;  
   public static final int CODE_BADGE_ID = 1;  

   private LayoutInflater mInflater;     
   private List<? extends Map<String, ?>> mData;  
   private int[] mTo;  
   private int mResource;  

   public final class ViewHolder{  
      public QuickContactBadge badge;  
      public TextView text;  
   }  

   public QuickContactAdapter(Context context, List<? extends Map<String, ?>> list, int resource,  
         int[] to) {  
      mInflater = LayoutInflater.from(context);  
      mData = list;  
      mTo = to;  
      mResource = resource;  
   }  

   @Override  
      public int getCount() {  
         return mData.size();  
      }  

   @Override  
      public Object getItem(int arg0) {  
         return null;  
      }  

   @Override  
      public long getItemId(int arg0) {  
         return 0;  
      }  

   @SuppressLint("NewApi")
      @Override  
      public View getView(int position, View convertView, ViewGroup parent) {  
         ViewHolder holder = null;  
         if (convertView == null) {  
            holder = new ViewHolder();   
            convertView = mInflater.inflate(mResource, null);  
            holder.text = (TextView)convertView.findViewById(mTo[CODE_TEXT_ID]);  
            holder.badge = (QuickContactBadge)convertView.findViewById(mTo[CODE_BADGE_ID]);  
            convertView.setTag(holder);  
         }  
         else {  
            holder = (ViewHolder)convertView.getTag();  
         }  

         holder.text.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);

         Profile profile = (Profile)mData.get(position).get("Profile");

         String distanceStr = "\n";
         Profile user = Utils.getUserProfile();
         if (user != null) {
            double distance = 
               Utils.distanceBetweenLocations(
                     Utils.getLocation(profile.getId()),
                     Utils.getLocation(user.getId()));
            if (distance != Double.POSITIVE_INFINITY) {
               distanceStr += String.format("%.2fm", distance);
            }
         }
         holder.text.setText(profile.getUsername() + distanceStr);  
         holder.badge.setOnClickListener(null);

         Bitmap bitmap = Utils.getBitmap(profile.getAvatar());
         if (bitmap != null) {
            holder.badge.setImageBitmap(bitmap);
         }
         else {
            holder.badge.setImageToDefault();
            holder.badge.setTag(profile.getAvatar());
            new NetworkUtils.DownloadImagesTask().execute(holder.badge);
         }
         return convertView;  
      }  
}  
