package org.sikuli.kinect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
         addNewMeasurementView((LengthMeasurement) m);         
      }
      repaint();
   }
   
   
   void addNewMeasurementView(final LengthMeasurement newMeasurement){
      final LengthMeasurement.View view = newMeasurement.new View();
      view.groundTruthLabel.addKeyListener(new KeyAdapter(){
         @Override
         public void keyPressed(KeyEvent e){
            System.out.println("removed");
            if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
               _frame.getMeasurements().remove(newMeasurement);
               overlay.remove(view);
               repaint();
            }
         }
      });

      overlay.add(view,0);

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
            
            addNewMeasurementView(newMeasurement);
            
//            final LengthMeasurement.View view = newMeasurement.new View();
//            view.groundTruthLabel.addKeyListener(new KeyAdapter(){
//               @Override
//               public void keyPressed(KeyEvent e){
//                  System.out.println("removed");
//                  if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE){
//                     _frame.getMeasurements().remove(newMeasurement);
//                     overlay.remove(view);
//                  }
//               }
//            });
//
//            overlay.add(view,0);
            
            
            
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