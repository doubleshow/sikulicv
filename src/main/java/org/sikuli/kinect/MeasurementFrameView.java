package org.sikuli.kinect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


class MeasurementFrameView extends JPanel {
   
   VideoFrameView frameView = new VideoFrameView();
   JPanel overlay = new JPanel();
   
   MeasurementFrameView(){
      setLayout(null);
      setFocusable(true);
      
      overlay.setSize(640,480);
      overlay.setOpaque(false);
      overlay.setLayout(null);
      
      add(frameView);
      add(overlay,0);
      
      setPreferredSize(new Dimension(640,480));
      setSize(640,480);
      
      setName("MeasurementFrameView");
      
      frameView.setSize(640,480);
      frameView.addMouseListener(selector);
      frameView.addMouseMotionListener(selector);
      frameView.setFocusable(true);
   }
   
   MeasurementFrame _frame;
   void setMeasurementFrame(MeasurementFrame frame){
      this._frame = frame;
      
      frameView.setVideoFrame(frame.getDualVideoFrame().getDepthFrame());
      
      overlay.removeAll();
      for (Measurement m : frame.getMeasurements()){        
         overlay.add(MeasurementViewFactory.createView(m),0);         
      }
      repaint();
   }
   
   
   LineSelector selector = new LineSelector();
   class LineSelector extends MouseAdapter {
      Point startPoint;
      Point endPoint;
      LengthMeasurement newMeasurement;
      
      final MouseAdapter userSelectionCompleted = new MouseAdapter(){
      };
      
      final MouseAdapter userSelectingEndPoint = new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e) {
            endPoint = e.getPoint();

            _frame.getMeasurements().add(newMeasurement);
            
            userEventAdapter = userSelectingStartPoint;
         }
         
         @Override
         public void mouseMoved(MouseEvent e) {
            newMeasurement.setEndPoint(e.getPoint());
         }

      };

      final MouseAdapter userSelectingStartPoint = new MouseAdapter(){
         @Override
         public void mouseClicked(MouseEvent e) {
            
            startPoint = e.getPoint();
            endPoint = e.getPoint();
            
            newMeasurement = new LengthMeasurement();
            newMeasurement.setStartPoint(startPoint);
            newMeasurement.setEndPoint(endPoint);
            overlay.add(MeasurementViewFactory.createView(newMeasurement),0);            
            repaint();

            requestFocus();
            
            userEventAdapter = userSelectingEndPoint;
         }
         

      };
      
      MouseAdapter userEventAdapter = userSelectingStartPoint;
      
      @Override
      public void mouseMoved(MouseEvent e) {
         userEventAdapter.mouseMoved(e);
         
      }

      @Override
      public void mouseClicked(MouseEvent e) {
         userEventAdapter.mouseClicked(e);
      }
      
      @Override
      public void mousePressed(MouseEvent e) {
         userEventAdapter.mousePressed(e);         
      }
      
      
   }

   

   
}