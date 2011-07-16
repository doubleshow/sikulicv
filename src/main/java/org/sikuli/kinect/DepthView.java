package org.sikuli.kinect;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

class VideoFrameView extends JPanel{

   JLabel imageLabel = new JLabel();
   VideoFrameView(){
      setLayout(null);
      imageLabel.setSize(640,480);
      setSize(640,480);
      add(imageLabel);
   }

   
   void setVideoFrame(VideoFrame f){
      setImage(f.getBufferedImage());
   }

   private void setImage(BufferedImage image){
      imageLabel.setIcon(new ImageIcon(image));
   }
}

class DepthView extends VideoFrameView{
   
   DepthValueView depthValueView = new DepthValueView();
   class DepthValueView extends JLabel {
      DepthValueView(){
         setBackground(Color.yellow);
         setForeground(Color.black);
         setOpaque(true);         
      }
      
      void setDepthValue(int depthValue){
         float distance = DepthCameraModel.depthToDistanceInCentimeters(depthValue);
         setText(""+depthValue + ":" + String.format("%.2f",distance));
         setSize(depthValueView.getPreferredSize());      
      }
   }
   
   DepthView(){
      super();
      
      add(depthValueView,0);
      setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
      
      addMouseMotionListener(new MouseMotionListener(){

         @Override
         public void mouseDragged(MouseEvent arg0) {
         }

         @Override
         public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            int depthValue = depthFrame.getDepthValue(p.x, p.y);
            depthValueView.setDepthValue(depthValue);
            depthValueView.setLocation(p.x-depthValueView.getWidth()/2,p.y-25);
         }
         
      });
   }
   
   DepthVideoFrame depthFrame;
   void setDepthFrame(DepthVideoFrame f){
      depthFrame = f;
      setVideoFrame(f);
   }
   
   
   //void setDepthFrame(DepthF)
}