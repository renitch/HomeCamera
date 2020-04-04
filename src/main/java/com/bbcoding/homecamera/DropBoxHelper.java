package com.bbcoding.homecamera;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;

public class DropBoxHelper {

   private static final String ACCESS_TOKEN = "";
   
   private final static Logger logger = LogManager.getLogger(DropBoxHelper.class);

   private static DbxClientV2 client;
   private static volatile DropBoxHelper instance;

   public static DropBoxHelper getInstance() {
      DropBoxHelper localInstance = instance;
      if (localInstance == null) {
         synchronized (DropBoxHelper.class) {
            localInstance = instance;
            if (localInstance == null) {
               instance = localInstance = new DropBoxHelper();
            }
         }
      }
      
      return localInstance;
   }

   private DropBoxHelper() {
      init();
   }
   
   private void init() {
      try {
         DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/renitch").build();
         client = new DbxClientV2(config, ACCESS_TOKEN);
         FullAccount account = client.users().getCurrentAccount();
         logger.info("Connected to DropBox. Account: " + account.getName().getDisplayName());
      } catch (DbxException e) {
         logger.error("Cannot connect to DropBox.", e);
      }
   }
   
   public static void uploadFile(File fileToUpload) {
      getInstance();

      try (InputStream in = new FileInputStream(fileToUpload)) {
         logger.info("Trying to upload temporary alert file " + fileToUpload.getAbsolutePath());
         FileMetadata metadata = client.files().uploadBuilder("/" + fileToUpload.getName()).uploadAndFinish(in);
         
         FileOutputStream os = new FileOutputStream("E:/" + fileToUpload.getName());
         client.files().downloadBuilder(metadata.getPathDisplay()).download(os);
         
         /*
         PathLinkMetadata plm = client.sharing().createSharedLink("/folder/test.json");
         String urlstr = plm.getUrl();
         System.out.println("url: " + urlstr);
         
         URL url = new URL(urlstr);
         InputStream urlis = url.openConnection().getInputStream();
         
         final int bufferSize = 1024;
         final char[] buffer = new char[bufferSize];
         final StringBuilder out = new StringBuilder();
         Reader inreader = new InputStreamReader(urlis, StandardCharsets.UTF_8);
         int charsRead;
         while((charsRead = inreader.read(buffer, 0, buffer.length)) > 0) {
             out.append(buffer, 0, charsRead);
         }
         String res =  out.toString();
         
         System.out.println("res: " + res);*/
      } catch (IOException | DbxException e) {
         logger.error("Cannot upload file ti DropBox.", e);
      }
   }
}
