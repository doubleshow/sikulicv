package org.sikuli.kinect;

import java.io.File;
import java.io.IOException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

class KinectDualVideoFrame implements DualVideoFrame {

   ColorVideoFrame colorFrame;
   DepthVideoFrame depthFrame;
   
   KinectDualVideoFrame(IplImage color, IplImage depth){
      colorFrame = new ColorVideoFrame(color);
      depthFrame = new DepthVideoFrame(depth);
   }
   
   KinectDualVideoFrame(){      
   }
   
   @Override
   public ColorVideoFrame getColorFrame() {
      return colorFrame;
   }

   @Override
   public DepthVideoFrame getDepthFrame() {
      return depthFrame;
   }
   
   static KinectDualVideoFrame read(File colorFile, File depthFile) throws IOException{
      KinectDualVideoFrame df = new KinectDualVideoFrame();
      df.colorFrame = ColorVideoFrame.read(colorFile);
      df.depthFrame = DepthVideoFrame.read(depthFile);      
      return df;
   }
}