package com.bbcoding.homecamera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CameraSettings {
   
   private final static Logger logger = LogManager.getLogger(CameraSettings.class);
   
   private static final String IP_EXTRACTION_REGEX = "^.*[^0-9](([0-9]{1,3}[\\.]){3}[0-9]{1,3}).*$";
   private static final String EXTERNAL_IP_CHECK_HOST= "http://checkip.amazonaws.com";
   
   private String cameraStreamUrl;
   private String cameraIp;
   private String myExternalIp;

   private static volatile CameraSettings instance;
   
   public static CameraSettings getInstance() {
      CameraSettings localInstance = instance;
      if (localInstance == null) {
         synchronized (CameraSettings.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new CameraSettings();
            }
         }
      }
      
      return localInstance;
   }
   
   private CameraSettings() {
      load();
   }
   
   public void load() {
      this.cameraStreamUrl = "rtsp://admin:@192.168.1.20:554";
      this.cameraIp = cameraStreamUrl.replaceFirst(IP_EXTRACTION_REGEX, "$1");
      this.myExternalIp = getExternalIp();
   }

   public String getCameraStreamUrl() {
      return cameraStreamUrl;
   }

   public void setCameraStreamUrl(String cameraStreamUrl) {
      this.cameraStreamUrl = cameraStreamUrl;
   }

   public String getCameraIp() {
      return cameraIp;
   }

   public void setCameraIp(String cameraIp) {
      this.cameraIp = cameraIp;
   }

   public String getMyExternalIp() {
      return myExternalIp;
   }

   public void setMyExternalIp(String myExternalIp) {
      this.myExternalIp = myExternalIp;
   }

   private String getExternalIp() {
      URL whatismyip = null;
      try {
         whatismyip = new URL(EXTERNAL_IP_CHECK_HOST);
      } catch (MalformedURLException e) {
         logger.error("URL for checking server's external IP is invalid: " + EXTERNAL_IP_CHECK_HOST, e);
         return "";
      }

      try (BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()))) {
         String ip = in.readLine();
         logger.info("My external IP is: " + ip);
         return ip.trim();
      } catch (IOException e) {
         logger.error("Server's external IP cannot be taken from service " + EXTERNAL_IP_CHECK_HOST, e);
      }
      
      return "";
   }

   @Override
   public String toString() {
      return "CameraSettings [cameraStreamUrl=" + cameraStreamUrl + ", cameraIp=" + cameraIp + ", myExternalIp="
            + myExternalIp + "]";
   }
}
