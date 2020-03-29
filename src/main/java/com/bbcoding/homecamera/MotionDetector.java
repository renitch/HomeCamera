package com.bbcoding.homecamera;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.HOGDescriptor;

public class MotionDetector extends Observable {

   static {
      nu.pattern.OpenCV.loadLocally();//.loadShared();
   }
  
   private static final HOGDescriptor HOG = new HOGDescriptor();
   private static final MatOfFloat DESCRIPTORS = HOGDescriptor.getDefaultPeopleDetector();

   //Threshold sensitivity for contours detection
   private int sensitivity = 5;
   
   //Minimal threshold for contour detection
   double minDetectionArea = 100;
   
   //Color settings
   private Scalar contoursColor        = new Scalar(0, 0, 255);
   private Scalar humanDetectedColor   = new Scalar(255, 255, 0);
   private Scalar motionRectangleColor = new Scalar(0, 255, 0);
   
   //Frame containing picture for analysis
   private Mat frame = new Mat();
   //Frames for analysis
   private Mat diffFrame = null;
   private Mat tempFrame = null;

   //Size of the analyzing frame
   private final Size frameSize       = new Size(256, 192);
   //private final Size frameSize       = new Size(384, 288);
   private int imageTypeForProcessing = CvType.CV_8UC1;

   //Final image containing analysis drawings 
   private Mat finalImage = new Mat();

   private boolean shouldInitFrames = true;
   
   private AlarmDetected alarm;

   public MotionDetector(Observer observer) {
      addObserver(observer);
      HOG.setSVMDetector(DESCRIPTORS);
   }
   
   public BufferedImage processImage(BufferedImage bufferedImage) {
      alarm = new AlarmDetected();
      
      setBufferedImage(bufferedImage);
      processOpenCV();
      processHumanDetection();
      
      if (alarm.alarmDetected()) {
         setChanged();
         notifyObservers(alarm);
      }
      
      return buildBufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());
   }
   
   private void setBufferedImage(BufferedImage bufferedImage) {
      frame = bufferedImageToMat(bufferedImage);
      Imgproc.resize(frame, frame, frameSize);
      finalImage = frame.clone();
   }
      
   private void processOpenCV() {
      final Mat outerBox = new Mat(frameSize, imageTypeForProcessing);
      Imgproc.cvtColor(frame, outerBox, Imgproc.COLOR_BGR2GRAY);
      Imgproc.GaussianBlur(outerBox, outerBox, new Size(3, 3), 0);

      if (shouldInitFrames) {
         diffFrame = new Mat(frameSize, imageTypeForProcessing);
         tempFrame = new Mat(frameSize, imageTypeForProcessing);
         diffFrame = outerBox.clone();
         
         shouldInitFrames = false;
      } else {
         Core.subtract(outerBox, tempFrame, diffFrame);
         Imgproc.threshold(diffFrame, diffFrame, sensitivity, 255, Imgproc.THRESH_BINARY);
         ArrayList<Rect> array = detectContours(diffFrame);
         if (array.size() > 0) {
            alarm.setMotionDetected();
            Iterator<Rect> it = array.iterator();
            while (it.hasNext()) {
               Rect obj = it.next();
               Imgproc.rectangle(finalImage, obj.br(), obj.tl(), motionRectangleColor, 1);
            }
         }
      }

      tempFrame = outerBox.clone();

   }

   private BufferedImage buildBufferedImage(int width, int height, int type) {
      BufferedImage finalImageBufferedImage = MatToBufferedImage(finalImage);
      
      Image tmp = finalImageBufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
      BufferedImage sourceCompatibleImage = new BufferedImage(width, height, type);

      Graphics2D g2d = sourceCompatibleImage.createGraphics();
      g2d.drawImage(tmp, 0, 0, null);
      g2d.dispose();

      return sourceCompatibleImage;
   }
   
   private ArrayList<Rect> detectContours(Mat outmat) {
      Mat hierarchy = new Mat();
      List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

      Imgproc.findContours(outmat.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

      int maxAreaIdx = -1;
      ArrayList<Rect> rectArray = new ArrayList<Rect>();

      for (int idx = 0; idx < contours.size(); idx++) { 
         Mat contour = contours.get(idx); 
         double contourarea = Imgproc.contourArea(contour); 
         if (contourarea > minDetectionArea) {
            maxAreaIdx = idx;
            rectArray.add(Imgproc.boundingRect(contours.get(maxAreaIdx)));
            Imgproc.drawContours(finalImage, contours, maxAreaIdx, contoursColor);
         }
      }

      hierarchy.release();

      return rectArray;
   }

   private void processHumanDetection() {
      final Size winStride = new Size(6, 6);
      final Size padding = new Size(8, 8);
      final MatOfRect foundPersons = new MatOfRect();
      final MatOfDouble foundWeights = new MatOfDouble();
      final Point rectPoint1 = new Point();
      final Point rectPoint2 = new Point();

      HOG.detectMultiScale(frame, foundPersons, foundWeights, 0.0, winStride, padding, 1.05, 1.5, false);

      if (foundPersons.rows() > 0) {
         alarm.setHumanDetected();
         List<Rect> rectList = foundPersons.toList();

         for (Rect rect : rectList) {
            // Draws rectangles around people
            rectPoint1.x = rect.x;
            rectPoint1.y = rect.y;
            rectPoint2.x = rect.x + rect.width;
            rectPoint2.y = rect.y + rect.height;
            // Draw rectangle around fond object
            Imgproc.rectangle(finalImage, rectPoint1, rectPoint2, humanDetectedColor, 2);
         }
      }
   }

   public static class AlarmDetected {
      private boolean motionDetected = false;
      private boolean humanDetected  = false;

      public AlarmDetected() {
         
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

      @Override
      public String toString() {
         return "AlarmDetected [motionDetected=" + motionDetected + ", humanDetected=" + humanDetected + "]";
      }
   }

   public static Mat bufferedImageToMat(BufferedImage bufferedImage) {
      if (bufferedImage != null) {
         try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
         } catch (IOException e) {
         }
      }
      return new Mat();
   }

   public static BufferedImage MatToBufferedImage(Mat image) {
      final MatOfByte bytemat = new MatOfByte();
      Imgcodecs.imencode(".jpg", image, bytemat);
      final byte[] bytes = bytemat.toArray();
      
      try (InputStream in = new ByteArrayInputStream(bytes)) {
         return ImageIO.read(in);
      } catch (IOException e) {
      }
      
      return null;
   }
}
