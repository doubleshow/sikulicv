package org.sikuli.kinect;

class FakeKinectDualFrameGrabber extends DualFrameGrabber {

   @Override
   void start() throws Exception {
   }

   @Override
   void stop() throws Exception {
   }

   @Override
   DualVideoFrame grab() throws Exception {      
//      DualVideoFrame df = new KinectDualVideoFrame(color, depth);      
//      return df;
      return null;
   }
   
}