package com.bbcoding.homecamera;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallbackAdapter;
import uk.co.caprica.vlcj.player.direct.format.RV32BufferFormat;

public class CameraStreamSource extends Observable {
   
   static {
      new NativeDiscovery().discover();
   }

   private final static Logger logger = LogManager.getLogger(CameraStreamSource.class);
   
   private final int width = 720;
   private final int height = 480;

   private final BufferedImage image;
   
   private String cameraStreamUrl;
   
   private static final MediaPlayerFactory MEDIA_PLAYER_FACTORY = new MediaPlayerFactory();
   private DirectMediaPlayer mediaPlayer;

   public CameraStreamSource(Observer observer, String cameraStreamUrl) {
      this.cameraStreamUrl = cameraStreamUrl;
      addObserver(observer);
      image = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(width, height);
      mediaPlayer = MEDIA_PLAYER_FACTORY.newDirectMediaPlayer(new IpCameraBufferFormatCallback(), new IpCameraRenderCallbackAdapter());
   }

   public void start() {
      logger.info("Starting player... Camera stream URL is " + cameraStreamUrl);
      if (mediaPlayer != null) {
         String [] options = {};
         mediaPlayer.playMedia(cameraStreamUrl, options);
      }
   }

   public void stop() {
      logger.info("Stopping player...");
      if (mediaPlayer != null) {
         mediaPlayer.stop();
      }
   }

   public void release() {
      logger.info("Releasing player resources...");
      if (mediaPlayer != null) {
         mediaPlayer.stop();
         mediaPlayer.release();
      }
      if (MEDIA_PLAYER_FACTORY != null) {
         MEDIA_PLAYER_FACTORY.release();
      }
   }

   private final class IpCameraBufferFormatCallback implements BufferFormatCallback {
      @Override
      public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
         return new RV32BufferFormat(width, height);
      }
   }
   
   private final class IpCameraRenderCallbackAdapter extends RenderCallbackAdapter {

      private IpCameraRenderCallbackAdapter() {
         super(new int[width * height]);
      }

      @Override
      protected void onDisplay(DirectMediaPlayer mediaPlayer, int[] rgbBuffer) {
         for(int i=0; i < rgbBuffer.length; i++){
            int argb = rgbBuffer[i];
            int b = (argb & 0xFF);
            int g = ((argb >> 8 ) & 0xFF);
            int r = ((argb >> 16 ) & 0xFF);
            int grey = (r + g + b + g) >> 2 ; //performance optimized - not real grey!
            rgbBuffer[i] = (grey << 16) + (grey << 8) + grey;
         }
         
         image.setRGB(0, 0, width, height, rgbBuffer, 0, width);
         
         setChanged();
         notifyObservers(image);
      }
   }
}
