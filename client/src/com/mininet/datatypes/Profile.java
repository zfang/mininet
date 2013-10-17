package com.mininet.datatypes;

import java.sql.Date;
import java.util.List;
import java.util.ArrayList;

import com.mininet.datatypes.Location;
import com.mininet.utils.Utils;

public class Profile implements Comparable<Profile> {
   String          id;
   String          username;
   String          password;  // Won't be serialized but should be de-serialized when registering
   String          avatar;
   String          gender;
   Date            birthdate;
   String          email;
   List<SocialNetwork>    socialnetworks;
   List<String>    interests;

   public transient double distance;

   public void setId( String id ) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }

   public void setUsername( String username ) {
      this.username = username;
   }

   public String getUsername() {
      return this.username;
   }

   public void setPassword( String password ){
      this.password = password;
   }

   public String getPassword(){
      return this.password;
   }

   public void setAvatar( String avatar ) {
      this.avatar = avatar;
   }

   public String getAvatar() {
      return this.avatar;
   }

   public void setGender( String gender ) {
      this.gender = gender;
   }

   public String getGender() {
      return this.gender;
   }

   public void setEmail( String email ) {
      this.email = email;
   }

   public String getEmail() {
      return this.email;
   }

   public void setBirthdate( String date ) {
      this.birthdate = Date.valueOf( date );
   }

   public Date getBirthdate() {
      return this.birthdate;
   }

   public void setSocialnetworks( List<SocialNetwork> socialnetworks ) {
      this.socialnetworks = socialnetworks;
   }

   public List<SocialNetwork> getSocialnetworks() {
      if (socialnetworks == null) {
         socialnetworks = new ArrayList<SocialNetwork>();
      }
      return socialnetworks;
   }

   public void setInterests( List<String> interests ) {
      this.interests = interests;
   }

   public List<String> getInterests() {
      if (interests == null) {
         interests = new ArrayList<String>();
      }
      return interests;
   }

   public int compareTo(Profile profile) {
      Profile user = Utils.getUserProfile();
      if (user == null) {
         return 0;
      }
      int distanceDiff = (int)
         (Utils.distanceBetweenLocations(
            Utils.getLocation(user.getId()),
            Utils.getLocation(this.getId())
            ) - 
         Utils.distanceBetweenLocations(
            Utils.getLocation(user.getId()),
            Utils.getLocation(profile.getId())
            ));
      if (distanceDiff != 0) {
         return distanceDiff;
      }
      else {
         return this.getUsername().compareTo(profile.getUsername());
      }
   }
}
