package org.sikuli.kinect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Commit;
import org.simpleframework.xml.core.Persister;

@Root
class Measurement{
   Date timestamp;
   
   class View extends JPanel {
      
      View(){
         add(new JLabel("base measurement"));
         setSize(200,200);
      }
   }
}


@Root
class Camera {
   
   @Attribute
   String name;
}

@Root
class MeasurementFile {
   
   @Element
   Camera camera;
   
   @ElementList
   List<MeasurementFrame> frames = new ArrayList<MeasurementFrame>();   
   
   @Attribute
   String name;  
   
   void saveToBundle(File bundlePath) throws Exception{
      //if (bundlePath)
      if (!bundlePath.exists()){
         bundlePath.mkdir();
      }
      
      Serializer serializer = new Persister();
      serializer.write(this, new File(bundlePath, "data.xml"));
      
      for (MeasurementFrame frame : frames){
         
         
         ColorVideoFrame color = frame.dualVideoFrame.getColorFrame();
         DepthVideoFrame depth = frame.dualVideoFrame.getDepthFrame();
         
         
         String colorFilename = frame.id + ".color.png";
         String depthFilename = frame.id + ".depth.bin";
         
         color.write(new File(bundlePath,colorFilename));
         depth.write(new File(bundlePath,depthFilename));

      }
      
   }
   
   static MeasurementFile readFromBundle(File bundlePath) throws Exception{
      Serializer serializer = new Persister();
      MeasurementFile mc = serializer.read(MeasurementFile.class, new File(bundlePath, "data.xml"));
      for (MeasurementFrame f : mc.frames){
         f.loadVideoFrameFromBundle(bundlePath);
      }
      return mc;
   }
}

@Root
class MeasurementFrame {
   
   MeasurementFrame(){
      id = UUID.randomUUID().toString();
   }
      
   @ElementList
   List<Measurement> measurements = new ArrayList<Measurement>();
   DualVideoFrame dualVideoFrame;
   
   @Attribute   
   String id;
   
   void loadVideoFrameFromBundle(File bundlePath) throws IOException{
      dualVideoFrame = KinectDualVideoFrame.read(new File(bundlePath, getColorImageFilename()),
            new File(bundlePath, getDepthImageFilename()));
   }
      
   String getColorImageFilename(){
      return id + ".color.png";
   }

   String getDepthImageFilename(){
      return id + ".depth.bin";
   }

}

class MeasurementViewFactory {
   
   static JComponent createView(Measurement lm){
      if (lm instanceof LengthMeasurement){
         return ((LengthMeasurement)lm).new View();
      }
      return null;
   }
   
}

@Root
class SelectionPoint {
   
   SelectionPoint(){
      
   }
   
   SelectionPoint(int x, int y){
      this.x = x;
      this.y = y;
   }
   @Attribute
   int x = 0;
   @Attribute
   int y = 0;
   
   Point asPoint(){
      return new Point(x,y);
   }
}

@Root
class LengthMeasurement extends Measurement {
   @Attribute
   int groundTruthLength;
   @Attribute
   int length;
   
   @Element
   SelectionPoint startPoint;
   
   @Element
   SelectionPoint endPoint;
        
   class View extends JPanel {
      
      View() {         
//         add(new JLabel("line measurement"));
         
         Rectangle r = new Rectangle(startPoint.asPoint());
         r.add(endPoint.asPoint());
         
         setBounds(r);
         setOpaque(false);
      }
      
      
      @Override
      public void paintComponent(Graphics g){
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D) g;
         g2d.setStroke(new BasicStroke(3.0f));
         g2d.setColor(Color.red);
         
         Rectangle r = getBounds();

         Ellipse2D c1 = new Ellipse2D.Double(0,0,10,10);
         g2d.draw(c1);               

         Ellipse2D c2 = new Ellipse2D.Double(r.width-10,r.height-10,10,10);
         g2d.draw(c2);               
         
         
         g2d.drawLine(5,5,r.width-5,r.height-5);             
      }
      
   }
   
}
