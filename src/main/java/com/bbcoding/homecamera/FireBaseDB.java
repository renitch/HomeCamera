package com.bbcoding.homecamera;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FireBaseDB {

   final static Logger logger = Logger.getLogger(FireBaseDB.class);
   
   private static final String DATABASE_URL = "https://homecam-12bdc.firebaseio.com";
   private String keyFilePath = "C:/myFirebaseKeys/homecam-12bdc-firebase-adminsdk-xnd69-9cf0ebabc9.json";

   private static final String SETTINGS_KEY = "settings";
   private static final String NEW_TOKEN_KEY = "newToken";
   private static final String ALL_TOKENS_KEY = "allTokens";
   
   private static volatile FireBaseDB instance;
   
   private String newToken;
   private List<String> allTokens = new ArrayList<>();
   
   public static FireBaseDB getInstance() {
      FireBaseDB localInstance = instance;
      if (localInstance == null) {
         synchronized (FireBaseDB.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new FireBaseDB();
            }
         }
      }
      
      return localInstance;
   }
   
   private FireBaseDB() {
      init();
   }
   
   private void init() {
      try (FileInputStream serviceAccount = new FileInputStream(keyFilePath)) {
          FirebaseOptions options = new FirebaseOptions.Builder()
             .setCredentials(GoogleCredentials.fromStream(serviceAccount))
             .setDatabaseUrl(DATABASE_URL)
             .build();
         FirebaseApp.initializeApp(options);
         
         createDataListeners();
      } catch (IOException e) {
         logger.error("Cannot connect to FIreBase DB.", e);
      }
   }
   
   private void createDataListeners() {
      DatabaseReference settingsRef = FirebaseDatabase.getInstance().getReference(SETTINGS_KEY);
      DatabaseReference newTokenRef = FirebaseDatabase.getInstance().getReference(NEW_TOKEN_KEY);
      DatabaseReference allTokensRef = FirebaseDatabase.getInstance().getReference(ALL_TOKENS_KEY);
      
      settingsRef.setValueAsync(CameraSettings.getInstance());
      
      settingsRef.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            Object document = dataSnapshot.getValue();
            logger.info("Settings data changed: " + document);
         }

         @Override
         public void onCancelled(DatabaseError error) {
         }
      });
      
      newTokenRef.addValueEventListener(new ValueEventListener() {
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            Object newTokenObject = dataSnapshot.getValue();
            if (newTokenObject instanceof String) {
               newToken = (String)newTokenObject;
               if (!newToken.isEmpty()) {
                  allTokens.add(newToken);
                  allTokensRef.setValueAsync(allTokens);
                  newTokenRef.setValueAsync(null);
               }
            }
            logger.info("New token data changed: " + newTokenObject);
         }

         @Override
         public void onCancelled(DatabaseError error) {
         }
      });
      
      allTokensRef.addValueEventListener(new ValueEventListener() {
         @SuppressWarnings({ "unchecked", "rawtypes" })
         @Override
         public void onDataChange(DataSnapshot dataSnapshot) {
            Object allTokensObject = dataSnapshot.getValue();
            logger.info("All tokens data changed: " + allTokensObject);
            if (allTokensObject instanceof List) {
               allTokens = (List)allTokensObject;
               logger.info("allTokens variable is now set to: " + allTokens);
            }
            if (allTokensObject == null) {
               allTokensRef.setValueAsync(allTokens);
               logger.info("allTokens variable is null, setting it to: " + allTokens);
            }
         }

         @Override
         public void onCancelled(DatabaseError error) {
         }
      });
   }
}
