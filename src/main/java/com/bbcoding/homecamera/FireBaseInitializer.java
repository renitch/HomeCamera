package com.bbcoding.homecamera;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

public class FireBaseInitializer {
   
   private final static Logger logger = LogManager.getLogger(FireBaseInitializer.class);
   
   private static final String DATABASE_URL = "https://homecam-12bdc.firebaseio.com";
   private String keyFilePath = "C:/myFirebaseKeys/homecam-12bdc-firebase-adminsdk-xnd69-9cf0ebabc9.json";

   private static volatile FireBaseInitializer instance;
   
   public static FireBaseInitializer getInstance() {
      FireBaseInitializer localInstance = instance;
      if (localInstance == null) {
         synchronized (FireBaseInitializer.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new FireBaseInitializer();
            }
         }
      }
      
      return localInstance;
   }

   private FireBaseInitializer() {
      init();
   }

   private void init() {
      try (FileInputStream serviceAccount = new FileInputStream(keyFilePath)) {
          FirebaseOptions options = new FirebaseOptions.Builder()
             .setCredentials(GoogleCredentials.fromStream(serviceAccount))
             .setDatabaseUrl(DATABASE_URL)
             .build();
          FirebaseApp.initializeApp(options);
      } catch (IOException e) {
         logger.error("Cannot connect to FIreBase DB.", e);
      }
   }
}
