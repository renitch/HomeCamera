package com.bbcoding.homecamera;

import java.util.Base64;

public class AlertInfo {

   private boolean motionDetected = false;
   private boolean humanDetected  = false;
   private String encodedImage = null;

   public AlertInfo() {
   }

   public boolean isMotionDetected() {
      return motionDetected;
   }

   public void setMotionDetected() {
      motionDetected = true;
   }

   public boolean isHumanDetected() {
      return humanDetected;
   }

   public void setHumanDetected() {
      humanDetected = true;
   };
   
   public boolean alarmDetected() {
      return motionDetected || humanDetected;
   }

   public String getEncodedImage() {
      return encodedImage;
   }

   public void setEncodedImage(byte [] finalImage) {
      encodedImage = Base64.getEncoder().encodeToString(finalImage);
   }
}
