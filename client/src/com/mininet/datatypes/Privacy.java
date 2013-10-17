package com.mininet.datatypes;

public class Privacy {
   public enum Level {Everyone, Friends, Me};
   String          id;
   Level           level;
   boolean         basic;
   boolean         location;
   boolean         detailed;

   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }
   public Level getLevel() {
      return level;
   }
   public void setLevel(Level level) {
      this.level = level;
   }
   public boolean getBasic() {
      return basic;
   }
   public void setBasic(boolean basic) {
      this.basic = basic;
   }
   public boolean getLocation() {
      return location;
   }
   public void setLocation(boolean location) {
      this.location = location;
   }
   public boolean getDetailed() {
      return detailed;
   }
   public void setDetailed(boolean detailed) {
      this.detailed = detailed;
   }
}
