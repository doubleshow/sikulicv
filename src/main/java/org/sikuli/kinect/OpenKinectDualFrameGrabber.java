package org.sikuli.kinect;


import com.googlecode.javacv.OpenKinectFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

class OpenKinectDualFrameGrabber extends DualFrameGrabber {
   
   OpenKinectFrameGrabber colorGrabber = new OpenKinectFrameGrabber(0);
   OpenKinectFrameGrabber depthGrabber = new OpenKinectFrameGrabber(0);

   
   @Override
   public void start() throws Exception{
      depthGrabber.setFormat("depth");
      depthGrabber.start();
      colorGrabber.start();
   }
   
   @Override
   public void stop() throws Exception{
      depthGrabber.stop();
      colorGrabber.stop();      
   }

   @Override
   public DualVideoFrame grab() throws Exception {      
      IplImage color = colorGrabber.grab();
      IplImage depth = depthGrabber.grab();      
      DualVideoFrame df = new KinectDualVideoFrame(color, depth);
      return df;
   }
   
}