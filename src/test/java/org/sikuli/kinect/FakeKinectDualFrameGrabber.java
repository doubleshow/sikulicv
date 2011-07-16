package org.sikuli.kinect;

import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.io.IOException;

class FakeKinectDualFrameGrabber extends DualFrameGrabber {

   DualVideoFrame df,df1;
   FakeKinectDualFrameGrabber() throws IOException{
      df = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));
      df1 = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));

      cvSmooth(df1.getColorFrame().getIplImage(), df1.getColorFrame().getIplImage(), CV_GAUSSIAN, 5);
      //cvSmooth(df1.getDepthFrame().getIplImage(), df1.getDepthFrame().getIplImage(), CV_GAUSSIAN, 5);
   }

   @Override
   void start() throws Exception {
   }

   @Override
   void stop() throws Exception {
   }

   @Override
   DualVideoFrame grab() throws Exception {      
      if (Math.random()>0.5){
         return df;
      }else
         return df1;
   }
   
}