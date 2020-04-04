package com.bbcoding.homecamera;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AlertSendingController {

   private final static Logger logger = LogManager.getLogger(AlertSendingController.class);
   
   private final static int minAlertIntervalMillis = 1 * 60 * 1000;
   private static long lastAlertMillis = 0;

   private static volatile AlertSendingController instance;

   public static AlertSendingController getInstance() {
      AlertSendingController localInstance = instance;
      if (localInstance == null) {
         synchronized (AlertSendingController.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new AlertSendingController();
            }
         }
      }
      
      return localInstance;
   }

   private AlertSendingController() {
   }

   public static void pushAlert(AlertInfo alertInfo) {
      long currentMillis = System.currentTimeMillis();
      if ((currentMillis - lastAlertMillis > minAlertIntervalMillis) /*&& alertInfo.isHumanDetected()*/) {
         logger.info("Sending alert!");
         
         String dateTimeString = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
         File temp = null;
         
         try (InputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(alertInfo.getEncodedImage()))) {
            temp = File.createTempFile("alert-" + dateTimeString + "-", ".jpg");
            
            BufferedImage im = ImageIO.read(in);
            ImageIO.write(im, "jpg", temp);
         } catch (IOException e) {
            logger.error("Cannot decode alert image", e);
         }
         FireBaseNotificationSender.sendMessage(new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()), temp);
         
         lastAlertMillis = currentMillis;
      } else {
         logger.info("Alert detected, but minimal alert sending interval is not passed.");
      }
   }
}
