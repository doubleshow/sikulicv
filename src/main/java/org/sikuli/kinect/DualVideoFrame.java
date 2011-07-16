package org.sikuli.kinect;

interface DualVideoFrame {   
   ColorVideoFrame getColorFrame();
   DepthVideoFrame getDepthFrame();   
}