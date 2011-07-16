package org.sikuli.kinect;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

class DualFrameDebugViewer extends JFrame {

   JLabel colorView = new JLabel();
   DepthView depthView = new DepthView();
   
   DualFrameDebugViewer(){
      colorView.setSize(new Dimension(640,480));
      depthView.setSize(new Dimension(640,480));
      setSize(new Dimension(1400,500));
      setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
      add(colorView);
      add(depthView);
   }
   
   DualFrameGrabber grabber;
   void setDualFrameGrabber(DualFrameGrabber grabber){
      this.grabber = grabber;
   }
   
   boolean running = false;   
   void start(){
      running = true;
      while (running){
      try {
         DualVideoFrame f = grabber.grab();         
         setDualVideoFrame(f);
      } catch (Exception e) {
         e.printStackTrace();
      }
      }
   }
   
   void stop(){
      running = false;
   }
   
   DualVideoFrame dualVideoFrame;
   void setDualVideoFrame(DualVideoFrame df){
      this.dualVideoFrame = df;
      setColorImage(df.getColorFrame().getBufferedImage());
      depthView.setDepthFrame(df.getDepthFrame());
   }
   
   void setColorImage(BufferedImage image){
      colorView.setIcon(new ImageIcon(image));
   }
   

}