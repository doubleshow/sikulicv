package org.sikuli.kinect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
   
   @Attribute
   int groundTruth;
   
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
   private
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
      
      for (MeasurementFrame frame : getMeasurementFrames()){
         
         
         ColorVideoFrame color = frame.getDualVideoFrame().getColorFrame();
         DepthVideoFrame depth = frame.getDualVideoFrame().getDepthFrame();
         
         
         String colorFilename = frame.id + ".color.png";
         String depthFilename = frame.id + ".depth.bin";
         
         color.write(new File(bundlePath,colorFilename));
         depth.write(new File(bundlePath,depthFilename));

      }
      
   }
   
   static MeasurementFile readFromBundle(File bundlePath) throws Exception{
      Serializer serializer = new Persister();
      MeasurementFile mc = serializer.read(MeasurementFile.class, new File(bundlePath, "data.xml"));
      for (MeasurementFrame f : mc.getMeasurementFrames()){
         f.loadVideoFrameFromBundle(bundlePath);
      }
      return mc;
   }

   public void setMeasurementFrames(List<MeasurementFrame> frames) {
      this.frames = frames;
   }

   public List<MeasurementFrame> getMeasurementFrames() {
      return frames;
   }

}

@Root
class MeasurementFrame {
   
   MeasurementFrame(){
      id = UUID.randomUUID().toString();
   }
      
   @ElementList
   private
   List<Measurement> measurements = new ArrayList<Measurement>();
   
   private DualVideoFrame dualVideoFrame;
   
   @Attribute   
   String id;
   
   void loadVideoFrameFromBundle(File bundlePath) throws IOException{
      setDualVideoFrame(KinectDualVideoFrame.read(new File(bundlePath, getColorImageFilename()),
            new File(bundlePath, getDepthImageFilename())));
   }
      
   String getColorImageFilename(){
      return id + ".color.png";
   }

   String getDepthImageFilename(){
      return id + ".depth.bin";
   }

   public List<Measurement> getMeasurements() {
      return measurements;
   }

   public void setDualVideoFrame(DualVideoFrame dualVideoFrame) {
      this.dualVideoFrame = dualVideoFrame;
   }

   public DualVideoFrame getDualVideoFrame() {
      return dualVideoFrame;
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
   public SelectionPoint(Point point) {
      x = point.x;
      y = point.y;
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
   int length;
   
   @Element
   SelectionPoint startPoint;
   
   @Element
   SelectionPoint endPoint;
   
   PropertyChangeSupport pcs = new PropertyChangeSupport(this);
   void setStartPoint(Point p){
      startPoint = new SelectionPoint(p);
      for (View view : views){
         view.refresh();
      }
   }
   
   void setEndPoint(Point p){
      endPoint = new SelectionPoint(p);
      for (View view : views){
         view.refresh();
      }
   }

   
   List<View> views = new ArrayList<View>();
        
   class View extends JPanel {

      GroundTruthLabel groundTruthLabel = new GroundTruthLabel();
      class GroundTruthLabel extends JLabel{
         
         GroundTruthLabel(){
            
            setName("GroundTruthLabel_" + startPoint.x + "_" + startPoint.y);

            
            setBackground(Color.yellow);
            setOpaque(true);
            
            addMouseListener(new MouseAdapter(){
               
               @Override
               public void mouseClicked(MouseEvent e){
                  if (e.getClickCount() == 2){
                     groundTruthEditor.setText(""+groundTruth);
                     groundTruthEditor.setVisible(true);
                     groundTruthEditor.requestFocus();
                  }
               }
               
            });
            
         }
      }
      
      GroundTruthEditor groundTruthEditor = new GroundTruthEditor();
      class GroundTruthEditor extends JTextField {
         
         void editCompleted(){
            try{
               groundTruth = Integer.parseInt(getText());
            }catch(NumberFormatException e){
            }
            setVisible(false);
            refresh();
         }
         
         GroundTruthEditor(){
            super(8);
            setName("GroundTruthEditor_" + startPoint.x + "_" + startPoint.y);
            
            addFocusListener(new FocusListener(){

               @Override
               public void focusGained(FocusEvent arg0) {
               }

               @Override
               public void focusLost(FocusEvent arg0) {
                  editCompleted();
               }
               
            });
            
            addKeyListener(new KeyAdapter(){
               
               @Override
               public void keyPressed(KeyEvent e){
                  if (e.getKeyCode() == KeyEvent.VK_ENTER){
                     editCompleted();
                  }
               }
            });

         }
         
      }
      
      MeasurementLine measurementLine = new MeasurementLine();
      class MeasurementLine extends JPanel {
         
         MeasurementLine(){
            setOpaque(false);
            setName("MeasurementLine_" + startPoint.x + "_" + startPoint.y);   
            

         }
         
         @Override
         public void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.setColor(Color.red);
            
            
            Rectangle r = getBounds();

            if (!dir){
               Ellipse2D c1 = new Ellipse2D.Double(0,0,10,10);
               g2d.draw(c1);               

               Ellipse2D c2 = new Ellipse2D.Double(r.width-10,r.height-10,10,10);
               g2d.draw(c2);
               
               g2d.drawLine(5,5,r.width-5,r.height-5);             
            }else{
               Ellipse2D c1 = new Ellipse2D.Double(0,r.height-10,10,10);
               g2d.draw(c1);               

               Ellipse2D c2 = new Ellipse2D.Double(r.width-10,0,10,10);
               g2d.draw(c2);
               
               g2d.drawLine(5,r.height-5,r.width-5,5);             
            }
         }
      }
      
      View() {         
         
//         add(new JLabel("line measurement"));
         
         
         setLayout(null);
         setSize(640,480);
         
         setOpaque(false);
         views.add(this);

         
         groundTruthEditor.setVisible(false);
         groundTruthEditor.setText("Text");
         groundTruthEditor.setSize(groundTruthEditor.getPreferredSize());
         groundTruthEditor.setLocation(startPoint.asPoint());
         
         add(groundTruthEditor);
         add(groundTruthLabel);
         add(measurementLine);
            
         refresh();
      }
      
      boolean dir = true;
      
      void refresh(){
         
         Rectangle r = new Rectangle(startPoint.asPoint());
         r.add(endPoint.asPoint());     
         r.grow(5,5);
         measurementLine.setBounds(r);
                  
         dir = (startPoint.x - endPoint.x) * (startPoint.y - endPoint.y) < 0;
                     
         groundTruthLabel.setText(""+groundTruth);
         groundTruthLabel.setSize(groundTruthLabel.getPreferredSize());
         groundTruthLabel.setLocation(startPoint.asPoint());
         
         repaint();
      }      
      

   }
   
}
