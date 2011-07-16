package org.sikuli.kinect;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import Jama.Matrix;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

//
//class WorldLocation extends Point3d{
//
//   public WorldLocation(double wx, double wy, double wz) {
//      super(wx,wy,wz);
//   }
//   
//   public WorldLocation() {
//      this(0,0,0);
//   }
//
//   public WorldLocation transform(Matrix R, Matrix T){
//      // convert w to RGB camera's world coordinate
//      double r[][] = R.getArray();
//      double t[][] = T.getArray();
//      
//      WorldLocation w = new WorldLocation();
//       w.x = r[0][0] * w.x + r[0][1] * w.y + r[0][2] * w.z + r[0][3] + t[0][0];
//       w.y = r[1][0] * w.x + r[1][1] * w.y + r[1][2] * w.z + r[1][3] + t[1][0];
//       w.z = r[2][0] * w.x + r[2][1] * w.y + r[2][2] * w.z + r[2][3] + t[2][0];
//       return w;
//   }
//   
//   
//}
//
//class ImageLocation extends Point{
//   //Calibration._F F;
//}
//
//class ColorImageLocation extends ImageLocation{
//
//   static ImageLocation createFromWorldLocation(WorldLocation loc){         
//      
//   }
//}
//
//class DepthImageLocation extends ImageLocation{
//   double cx;
//   double cy;
//   double fx;
//   double fy;
//   
//   int depthValue;
//   public DepthImageLocation(int x, int y, int depthValue) {
//      super(x,y);
//      depthValue = depthValue;
//      cx = calibration.F.cx;
//      cy = calibration.F.cy;
//      fx = calibration.F.fx;
//      fy = calibration.F.fy;
//   }
//
//   WorldLocation toWorldLocation(){
//      double wz = Calibration.depthToDistanceInCentimeters(depthValue)*0.01;   // centimeter -> meter
//      double wx = (x - cx) * wz / fx;
//      double wy = (y - cy) * wz / fy;      
//      return new WorldLocation(wx,wy,wz);         
//   }        
//   
//}


public class CalibratedVideoFrame implements VideoFrame {
   
   DualVideoFrame dualVideoFrame;
   Calibration calibration;
   
   CalibratedVideoFrame(DualVideoFrame df, Calibration calibration){
      this.calibration = calibration;
      this.dualVideoFrame = df;      
   }

   @Override
   public IplImage getIplImage() {
      return null;
   }

   BufferedImage calibratedImage = new BufferedImage(640,480,BufferedImage.TYPE_INT_RGB);
   
   @Override
   public BufferedImage getBufferedImage() {
      
       BufferedImage colorImage = dualVideoFrame.getColorFrame().getBufferedImage();
      
      for (int dy=0;dy<480;++dy){   
         for (int dx=0;dx<640;++dx){
                        
            int depthValue = dualVideoFrame.getDepthFrame().getDepthValue(dx,dy);
            Point3d w = calibration.depthCameraModel.imageLocationToWorldLocation(dx,dy,depthValue);
            
            Point3d w1 = w.transform(calibration.transform.R,calibration.transform.T);
            
            // project to RGB camera's image coordinate
            Point q = calibration.colorCameraModel.worldLocationToImageLocation(w1);

             int cx = q.x;
             int cy = q.y;
             // boundary clamp
             if (cx >= 0 && cx < 640 && cy >=0 && cy < 480){
                calibratedImage.setRGB(dx,dy,colorImage.getRGB(cx,cy));
             }else{
                calibratedImage.setRGB(dx,dy,Color.green.getRGB());
             }
         }
      }
           
      return calibratedImage;
   }

   @Override
   public void write(File output) throws IOException {     
   }

}
