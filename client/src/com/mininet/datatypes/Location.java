package com.mininet.datatypes;

public class Location {
   String          pid;
   double          longitude;
   double          latitude;

   public void setPid( String pid ) {
      this.pid = pid;
   }

   public String getPid() {
      return this.pid;
   }

   public void setLongitude( double longitude ) {
      this.longitude = longitude;
   }

   public double getLongitude() {
      return this.longitude;
   }

   public void setLatitude( double latitude ) {
      this.latitude = latitude;
   }

   public double getLatitude() {
      return this.latitude;
   }

}
