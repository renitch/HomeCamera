package com.bbcoding.homecamera;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CameraController extends Observable {
	
   private final static Logger logger = LogManager.getLogger(CameraController.class);
   
	private volatile boolean hostReady = false;
	private volatile boolean shouldPingHost = true;
	
	private volatile boolean currentReachableValue = false;
	private volatile boolean previousReachableValue = false;
	
	private String cameraHostIp;
	
	public CameraController(Observer observer, String cameraHostIp) {
	   this.cameraHostIp = cameraHostIp;
	   addObserver(observer);
	}
	
   public void startController() {
      logger.info("Start controller...");
      synchronized (CameraController.class) {
         shouldPingHost = true;
         currentReachableValue = false;
         previousReachableValue = false;
      }
      startPingThread();
   }

   public void stopController() {
      logger.info("Stop controller...");
	   synchronized (CameraController.class) {
	      shouldPingHost = false;
         currentReachableValue = false;
         previousReachableValue = false;
	   }
	}
	
	private void startPingThread() {
	   Runnable r = new Runnable() {
	      @Override
	      public void run() {
	         checkCameraHost();
	      }
	   };

	   Thread thread = new Thread(r);
	   thread.start();	   
	}
	
	private void checkCameraHost() {
      try {
         while (shouldPingHost) {
            InetAddress address = InetAddress.getByName(cameraHostIp);
            hostReady = address.isReachable(1000);
            logger.info("Host is reachable: " + hostReady);
            
            currentReachableValue = hostReady;
            if (currentReachableValue != previousReachableValue) {
               setChanged();
               if (currentReachableValue) {
                  notifyObservers(new HostAccessible());
                  logger.info("Camera became accessible...");
               } else {
                  notifyObservers(new HostOffline());
                  logger.info("Camera went offline...");
               }
            }
            
            Thread.sleep(1000);
            previousReachableValue = currentReachableValue;
         }
         Thread.sleep(1);
      } catch (IOException | InterruptedException e) {
         logger.error("Exception while checking camera host ", e);
      }
	}
	
	public class HostAccessible {
	}

   public class HostOffline {
   }
}
