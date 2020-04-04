package com.bbcoding.homecamera;

import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HomeCamera implements Observer {
   
   private final static Logger logger = LogManager.getLogger(HomeCamera.class);

   private final JFrame mainFrame = new JFrame();
   private final JLabel videoPanel = new JLabel();

   private final CameraSettings cameraSettings = CameraSettings.getInstance();
   
   private final CameraController cameraController;
   private final MotionDetector motionDetector;
   private final CameraStreamSource cameraStreamSource;
   
   private static final String TITLE_TEMPLATE = "Home Camera %s";
   
   static {
      FireBaseInitializer.getInstance();
      FireBaseDB.getInstance();
   }
   
   private HomeCamera() {
      motionDetector = new MotionDetector(this);
      cameraController = new CameraController(this, cameraSettings.getCameraIp());
      cameraStreamSource = new CameraStreamSource(this, cameraSettings.getCameraStreamUrl());

      createMainFrame();
      start();
   }
   
   private void createMainFrame() {
      mainFrame.setTitle(String.format(TITLE_TEMPLATE, cameraSettings.getCameraIp()));
      mainFrame.setLocation(100, 100);       
      mainFrame.setSize(1050, 600);
      mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      mainFrame.setVisible(true);
      mainFrame.addWindowListener(new WindowClosingListener());
      mainFrame.setContentPane(videoPanel);
   }
   
   
   private void start() {
      cameraController.startController();      
   } 
   
   private void stop() {
      cameraController.stopController(); 
      cameraStreamSource.stop();
   } 

   private void release() {
      cameraStreamSource.release();
   } 

   @Override
   public void update(Observable sender, Object parameter) {
      if (parameter instanceof BufferedImage) {
         ImageIcon image = new ImageIcon(motionDetector.processImage((BufferedImage)parameter));

         videoPanel.setIcon(image);
         videoPanel.repaint();
      }
      
      if (parameter instanceof CameraController.HostAccessible) {
         logger.info("Start recording from camera source...");
         cameraStreamSource.start();
      }

      if (parameter instanceof CameraController.HostOffline) {
         logger.info("Stop camera source...");
         cameraStreamSource.stop();
      }
      
      if (parameter instanceof AlertInfo) {
         logger.info(parameter);
         AlertSendingController.pushAlert((AlertInfo)parameter);
      }
   }

   private class WindowClosingListener implements java.awt.event.WindowListener {
      @Override
      public void windowClosing(final java.awt.event.WindowEvent e) {
          synchronized (HomeCamera.this) {
             stop();
             release();
          }
      }
      
      @Override
      public void windowClosed(final java.awt.event.WindowEvent e) { }
      @Override
      public void windowActivated(final java.awt.event.WindowEvent e) { }
      @Override
      public void windowDeactivated(final java.awt.event.WindowEvent e) { }
      @Override
      public void windowDeiconified(final java.awt.event.WindowEvent e) { }
      @Override
      public void windowIconified(final java.awt.event.WindowEvent e) { }
      @Override
      public void windowOpened(final java.awt.event.WindowEvent e) { }
   }
   
   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {      
         public void run() {
            new HomeCamera();
         }
      });
   }
}
