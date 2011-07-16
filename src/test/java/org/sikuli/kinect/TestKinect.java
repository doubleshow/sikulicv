package org.sikuli.kinect;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import Jama.Matrix;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

//@RunWith(Enclosed.class)

class MeasurementFileListView extends JList {
   
   MeasurementFileListView(MeasurementFile measurementCase){
      this();
      setListData(measurementCase.frames.toArray());        
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
         
         _thumbView.setPreferredSize(new Dimension(100,100));        
         
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
         VideoFrame vFrame = frame.dualVideoFrame.getDepthFrame();
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


class MainFrame extends JFrame {
   
   MeasurementFileListView fileListView;
   MeasurementFrameView frameView;
   
   MainFrame(){
      
      fileListView = new MeasurementFileListView();
      fileListView.addListSelectionListener(new ListSelectionListener(){

         @Override
         public void valueChanged(ListSelectionEvent e) {
            int index = fileListView.getSelectedIndex();
            selectFrameByIndex(index);
         }
         
      });
      
      
      frameView = new MeasurementFrameView();
      
      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileListView, frameView);
      add(splitPane);
      
      
      setResizable(false);
   }
   
   void selectFrameByIndex(int index){
      frameView.setMeasurementFrame(file.frames.get(index));      
   }
   
   MeasurementFile file;
   void setMeasurementCase(MeasurementFile file){
      this.file = file;
      fileListView.setListData(file.frames.toArray());
      fileListView.setSelectedIndex(0);
      selectFrameByIndex(0);
   }
   
}

class MeasurementFrameView extends JPanel {
   
   VideoFrameView frameView = new VideoFrameView();
   JPanel overlay = new JPanel();
   
   MeasurementFrameView(){
      setLayout(null);
      
      overlay.setSize(640,480);
      overlay.setOpaque(false);
      overlay.setLayout(null);
      
      add(frameView);
      add(overlay,0);
      
      setPreferredSize(new Dimension(640,480));
      setSize(640,480);
   }
   
   void setMeasurementFrame(MeasurementFrame frame){
      
      frameView.setVideoFrame(frame.dualVideoFrame.getDepthFrame());
      frameView.setSize(640,480);
      
      overlay.removeAll();
      for (Measurement m : frame.measurements){        
         overlay.add(MeasurementViewFactory.createView(m),0);         
      }
      repaint();
   }
   
}

//class MeasurementView extends JPanel {
   
   
//}

//class MeasurementFrameView extends DualFrameDebugViewer {
//   MeasurementFrame frame; 
//
//   MeasurementFrameView(MeasurementFrame frame){
//      setDualVideoFrame(frame.dualVideoFrame);
//
//      
//      for (Measurement m : frame.measurements){        
//         depthView.add(MeasurementViewFactory.createView(m),0);         
//      }
//   }     
//}


public class TestKinect {

   @Test
   public void testOpenKinectDualFrameGrabber() throws Exception {

      OpenKinectDualFrameGrabber grabber = new OpenKinectDualFrameGrabber();
      grabber.start();

      DualFrameDebugViewer viewer = new  DualFrameDebugViewer();
      viewer.setDualFrameGrabber(grabber);
      viewer.setVisible(true);
      viewer.start();

      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }
   }

   @Test
   public void testProbeDistance() throws IOException{

      DualVideoFrame df = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));

      DepthVideoFrame f = df.getDepthFrame();

      IplImage img = f.getIplImage();
      ByteBuffer bb = img.getByteBuffer();
      ShortBuffer b = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();

      while (b.hasRemaining())
         assertThat((int)b.get(), lessThan(2048));
   }


   @Test
   public void testOpenKinectRecorder() throws Exception {

      OpenKinectDualFrameGrabber grabber = new OpenKinectDualFrameGrabber();
      grabber.start();


      DualVideoFrame df = grabber.grab();
      df.getDepthFrame().write(new File("depth.bin"));
      df.getColorFrame().write(new File("color.png"));

      DualVideoFrame dfin = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));

      DualFrameDebugViewer viewer = new  DualFrameDebugViewer();
      viewer.setVisible(true);
      viewer.setDualVideoFrame(dfin);

      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }
   }


   @Test
   public void testDualFrameDebugViewer() throws Exception{

      ColorVideoFrame color = mock(ColorVideoFrame.class);
      DepthVideoFrame depth = mock(DepthVideoFrame.class);

      when(color.getBufferedImage()).thenReturn(ImageIO.read(new File("color.png")));
      when(depth.getBufferedImage()).thenReturn(ImageIO.read(new File("depth.png")));

      DualVideoFrame dualFrame = mock(DualVideoFrame.class);
      when(dualFrame.getColorFrame()).thenReturn(color);
      when(dualFrame.getDepthFrame()).thenReturn(depth);

      DualFrameGrabber grabber = mock(DualFrameGrabber.class);
      when(grabber.grab()).thenReturn(dualFrame);

      DualFrameDebugViewer viewer = new  DualFrameDebugViewer();
      viewer.setDualFrameGrabber(grabber);
      viewer.setVisible(true);
      viewer.start();



      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }

   }

   static public class CalibrationLoadFromJSONTest{

      //      private ColorCameraModel colorCameraModel 
      //      = new ColorCameraModel(4.6831326560522564e+02,4.6491638429833148e+02,3.4281546567751963e+02, 2.5035843764290559e+02);
      //      private DepthCameraModel depthCameraModel 
      //      = new DepthCameraModel(4.8378065692480374e+02,4.8064891800790127e+02,3.5888505420667735e+02, 2.4766889087469457e+02);
      //
      //      CameraModelTransform transform = new CameraModelTransform();
      //      double[][] rawR = new double[][]{ 
      //            {9.9984030034806526e-01, 1.9232056628715403e-03, -1.7767247391449924e-02, 0},
      //               {-2.2409877935182822e-03,9.9983756997140416e-01, -1.7883278429492212e-02, 0},
      //               {1.7729968234601757e-02, 1.7920238660680227e-02, 9.9968220613990344e-01,0},
      //               {0,0,0,1}};
      //            
      //      double[][] rawT = new double[][]{
      //            {3.1157357150415746e-02f, -9.4281476188136612e-05f,-4.2779889235526058e-02f,1}};
      //
      //      
      @Test
      public void setUp(){

      }





      @Test
      public void testLoadCalibrationYML() throws IOException{         
         File file = new File("kinect_calibration.yml1");
         Calibration c = Calibration.loadFromFile(file);
      }


   }
   

   static public class MeasurementSaveLoad {

      private Camera camera = new Camera();
      private MeasurementFile measurementCase = new MeasurementFile();
      private MeasurementFrame measurementFrame = new  MeasurementFrame();
      private MeasurementFrame anotherMeasurementFrame = new  MeasurementFrame();
      private LengthMeasurement lengthMeasurement = new LengthMeasurement();
      private LengthMeasurement anotherLengthMeasurement = new LengthMeasurement();

      @Before
      public void setUp() throws IOException{
         lengthMeasurement.length = 100;
         lengthMeasurement.groundTruthLength = 110;
         lengthMeasurement.startPoint = new SelectionPoint(50,50);
         lengthMeasurement.endPoint = new SelectionPoint(150,250);

         anotherLengthMeasurement.length = 100;
         anotherLengthMeasurement.groundTruthLength = 110;
         anotherLengthMeasurement.startPoint = new SelectionPoint(250,50);
         anotherLengthMeasurement.endPoint = new SelectionPoint(350,150);
         
         
         camera.name = "My Kinect";

         measurementFrame.measurements.add(lengthMeasurement);
         measurementFrame.measurements.add(anotherLengthMeasurement);
         measurementFrame.dualVideoFrame = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));
         
         anotherMeasurementFrame.measurements.add(anotherLengthMeasurement);
         anotherMeasurementFrame.dualVideoFrame = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));

         measurementCase.camera = camera;
         measurementCase.name = "iPad";
         measurementCase.frames.add(measurementFrame);
         measurementCase.frames.add(anotherMeasurementFrame);

      }

      
      @Test
      public void testMain() throws InterruptedException{
         
         MainFrame f = new MainFrame();
         f.setMeasurementCase(measurementCase);
         f.setVisible(true);
         f.pack();
         
         Object lock = new Object();
         synchronized(lock){
            lock.wait();
         }         

      }


//      @Test
//      public void testMeasurementView() throws InterruptedException {
//
//         MeasurementFrameView view = new MeasurementFrameView(measurementFrame);           
//         view.setVisible(true);
//
//         Object lock = new Object();
//         synchronized(lock){
//            lock.wait();
//         }         
//      }

//      @Test
//      public void testSaveLoadAndView() throws Exception {
//         File bundlePath = createTempDirectory();
//
//         measurementCase.saveToBundle(bundlePath);
//         
//         MeasurementFile mc = MeasurementFile.readFromBundle(bundlePath);
//         
//
//         MeasurementFrameView view = new MeasurementFrameView(mc.frames.get(0));           
//         view.setVisible(true);
//
//         Object lock = new Object();
//         synchronized(lock){
//            lock.wait();
//         }         
//      }

      public static File createTempDirectory()
      throws IOException
      {
         final File temp;

         temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

         if(!(temp.delete()))
         {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
         }

         if(!(temp.mkdir()))
         {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
         }

         return (temp);
      }

      @Test
      public void testSaveToLoadFromBundlePath() throws Exception{
         //File bundlePath = createTempDirectory();

         File bundlePath = new File("testSave.kmx");
         if (bundlePath.exists()){
            for (File file : bundlePath.listFiles())
               file.delete();            
            bundlePath.delete();
         }

         measurementCase.saveToBundle(bundlePath);

         assertTrue(bundlePath.exists());
         assertTrue(new File(bundlePath,"data.xml").exists());

         // should have the same number of images as the number of frames
         File[] pngs = bundlePath.listFiles(new FilenameFilter(){

            @Override
            public boolean accept(File f, String str) {
               return str.endsWith("png");
            }            
         });

         assertEquals(pngs.length, measurementCase.frames.size());

         MeasurementFile loadedCase = MeasurementFile.readFromBundle(bundlePath);
      }


      @Test
      public void testSaveMeasurementsAsXMLFiles() throws Exception{
         Serializer serializer = new Persister();         
         serializer.write(measurementCase, new File("test.xml"));
         MeasurementFile readBack = serializer.read(MeasurementFile.class, new File("test.xml"));
         serializer.write(readBack, new File("test1.xml"));
      }


   }

   static public class CalibratedImageTestCase {

      DualVideoFrame df;

      @Before
      public void setUp() throws IOException{
         df = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));         
      }


      @Test
      public void testViewCalibratedVideoFrameWithCalibrationFromFile() throws InterruptedException, IOException{

         File file = new File("kinect_calibration.yml1");
         Calibration c = Calibration.loadFromFile(file);
         CalibratedVideoFrame f = new CalibratedVideoFrame(df,c);

         VideoFrameView v = new VideoFrameView();
         v.setVideoFrame(f);

         JFrame frame = new JFrame();
         frame.add(v);
         frame.setSize(640,480);
         frame.setVisible(true);

         Object lock = new Object();
         synchronized(lock){
            lock.wait();
         }

      }


      @Test
      public void testCreateCalibratedVideoFrame() throws InterruptedException{

         Calibration c = new Calibration();


         ColorCameraModel colorCameraModel 
         = new ColorCameraModel(4.6831326560522564e+02,4.6491638429833148e+02,3.4281546567751963e+02, 2.5035843764290559e+02);
         DepthCameraModel depthCameraModel 
         = new DepthCameraModel(4.8378065692480374e+02,4.8064891800790127e+02,3.5888505420667735e+02, 2.4766889087469457e+02);

         CameraModelTransform transform = new CameraModelTransform();
         double[][] rawR = new double[][]{ 
               {9.9984030034806526e-01, 1.9232056628715403e-03, -1.7767247391449924e-02, 0},
               {-2.2409877935182822e-03,9.9983756997140416e-01, -1.7883278429492212e-02, 0},
               {1.7729968234601757e-02, 1.7920238660680227e-02, 9.9968220613990344e-01,0},
               {0,0,0,1}};

         double[][] rawT = new double[][]{
               {3.1157357150415746e-02f, -9.4281476188136612e-05f,-4.2779889235526058e-02f,1}};
         Matrix R_rgb = (new Matrix(rawR)).transpose();
         Matrix T_rgb = (new Matrix(rawT)).transpose();

         double rawI[][] = {{-1,0,0,0},{0,1,0,0},{0,0,-1,0},{0,0,0,1}};
         Matrix I = new Matrix(rawI);
         Matrix Iinv = I.inverse();
         Matrix Rinv = R_rgb.inverse();
         transform.R = Iinv.times(Rinv).times(I);         
         transform.T = I.times(T_rgb);


         c.colorCameraModel = colorCameraModel;
         c.depthCameraModel = depthCameraModel;
         c.transform = transform;


         CalibratedVideoFrame f = new CalibratedVideoFrame(df,c);


         VideoFrameView v = new VideoFrameView();
         v.setVideoFrame(f);

         JFrame frame = new JFrame();
         frame.add(v);
         frame.setSize(640,480);
         frame.setVisible(true);

         Object lock = new Object();
         synchronized(lock){
            lock.wait();
         }

      }


   }


   static public class DepthImageTestCase {

      DualVideoFrame df;

      @Before
      public void setUp() throws IOException{
         df = KinectDualVideoFrame.read(new File("color.png"), new File("depth.bin"));         
      }

      @Test
      public void testRawValuesInTheCorrectRange() {

         DepthVideoFrame f = df.getDepthFrame();

         ShortBuffer b = f.getShortBuffer();
         while (b.hasRemaining()){
            int i = (int)b.get();
            assertThat(i, lessThan(2048));
            assertThat(i, greaterThan(0));
         }

         b.rewind();
         for (int y = 0; y < 480; ++ y){
            for (int x = 0; x < 640; ++x){

               int d = f.getDepthValue(x,y);
               int p = b.get();

               assertThat(d, lessThan(2048));
               assertThat(d, greaterThan(0));
               assertThat(d, equalTo(p));
            }
         }
      }


      @Test
      public void testProbeDepthImage() throws InterruptedException{
         DualFrameDebugViewer viewer = new  DualFrameDebugViewer();         
         viewer.setVisible(true);         
         viewer.setDualVideoFrame(df);

         Object lock = new Object();
         synchronized(lock){
            lock.wait();
         }
      }


   }

}


