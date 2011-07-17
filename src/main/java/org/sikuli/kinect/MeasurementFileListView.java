package org.sikuli.kinect;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

class MeasurementFileListView extends JList {
   
   MeasurementFileListView(MeasurementFile measurementCase){
      this();
      setListData(measurementCase.getMeasurementFrames().toArray());        
   }
   
   MeasurementFileListView(){      
      setCellRenderer(new MeasurementFrameCellRenderer());
   }
   
   
   class VideoFrameThumbView extends VideoFrameView{
      
      
      @Override
      public void paintChildren(Graphics g0){
         Graphics2D g = (Graphics2D) g0;
           
           Dimension size = getSize();
           Dimension contentSize = new Dimension(640,480);
          
           float scalex = 1f* size.width / contentSize.width;
           float scaley = 1f* size.height / contentSize.height;
           float minscale = Math.min(scalex,scaley);

           int height = (int) (contentSize.height * minscale);
           int width = (int) (contentSize.width * minscale);

           int x = size.width/2 - width/2;
           int y = size.height/2 - height/2; 
           
           g.translate(x,y);
           g.scale(minscale,minscale);
           
           super.paintChildren(g);
      }
   }
   

   class MeasurementFrameCellRenderer extends JPanel implements ListCellRenderer {
      VideoFrameView _thumbView = new VideoFrameThumbView();
      
      MeasurementFrameCellRenderer(){         
         setLayout(new GridBagLayout());
         setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         
         _thumbView.setPreferredSize(new Dimension(140,140));        
         
         GridBagConstraints c = new GridBagConstraints();
         c.anchor = GridBagConstraints.CENTER;
         add(_thumbView,c);
         validate();
      }

      public Component getListCellRendererComponent(
        JList list,              // the list
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus)    // does the cell have focus
      {
         
         MeasurementFrame frame = (MeasurementFrame) value;
         VideoFrame vFrame = frame.getDualVideoFrame().getDepthFrame();
         _thumbView.setVideoFrame(vFrame);        
         
         setPreferredSize(new Dimension(150,150));
         
         if (isSelected){
            setBorder(BorderFactory.createLineBorder(Color.red, 5));
         }else{
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         }
         
          return this;
      }

 
  }
}