package com.bbcoding.homecamera;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;

public class FireBaseNotificationSender {

   private final static Logger logger = LogManager.getLogger(FireBaseNotificationSender.class);

   private static volatile FireBaseNotificationSender instance;

   public static FireBaseNotificationSender getInstance() {
      FireBaseNotificationSender localInstance = instance;
      if (localInstance == null) {
         synchronized (FireBaseNotificationSender.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new FireBaseNotificationSender();
            }
         }
      }
      
      return localInstance;
   }

   private FireBaseNotificationSender() {
   }
   
   public static void sendMessage(String time, File imageFile) {
      List<String> registrationTokens = FireBaseDB.getAllTokens();

      DropBoxHelper.uploadFile(imageFile);
      /*for (String token : registrationTokens) {
         try {
            sendPersonal(new PushNotifyConf("title", "Alert detected at: " + time, "", "", "100"), token, imageFile);
         } catch (Throwable e) {
            logger.error("Cannot send message to " + token, e);
         }
      }*/
   }

   public static String sendPersonal(PushNotifyConf conf, String clientToken, File pictureObject) throws InterruptedException, ExecutionException {
      Message message = Message.builder().setToken(clientToken)
            .setNotification(new Notification("myTitle", /*pictureObject.toString()*/"/" + pictureObject.getName(), "myImageUrl"))
            .setWebpushConfig(WebpushConfig.builder()
                  .putHeader("ttl", conf.getTtlInSeconds())
                  .setNotification(createBuilder(conf, pictureObject).build())
                  .build())
            .build();
      /*Message message = Message.builder()
            .setWebpushConfig(WebpushConfig.builder()
                .setNotification(new WebpushNotification(
                    "$GOOG up 1.43% on the day",
                    "$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.",
                    "https://my-server/icon.png"))
                .build())
            .setTopic("industry-tech")
            .build();*/
      /*Message message = Message.builder()
            .setAndroidConfig(AndroidConfig.builder()
                .setTtl(3600 * 1000) // 1 hour in milliseconds
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                    .setTitle("$GOOG up 1.43% on the day")
                    .setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.")
                    .setIcon("stock_ticker_update")
                    .setColor("#f45342")
                    .build())
                .build())
            .setTopic("industry-tech")
            .build();*/      
      String response = FirebaseMessaging.getInstance()
            .sendAsync(message)
            .get();
      return response;
   }

   private static WebpushNotification.Builder createBuilder(PushNotifyConf conf, Object pictureObject){
      WebpushNotification.Builder builder = WebpushNotification.builder();
      builder.setBody(conf.getBody()).setTitle(conf.getTitle());
      Map<String, Object> data = new HashMap<>();
      //data.put("picture_url", pictureObject);
      //builder.putAllCustomData(data);
      /*builder.addAction(new WebpushNotification
                .Action(conf.getClick_action(), "Открыть"))
                .setImage(conf.getIcon())
                .setTitle(conf.getTitle())
                .setBody(conf.getBody());*/
      return builder;
   }
}
