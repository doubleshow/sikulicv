package org.sikuli.kinect;

abstract class DualFrameGrabber {  
   abstract void start() throws Exception;
   abstract void stop() throws Exception;
   abstract DualVideoFrame grab() throws Exception;   
}