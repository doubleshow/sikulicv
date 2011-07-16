package org.sikuli.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.sikuli.cv.TemplateFinder;
import org.sikuli.cv.BaseTemplateFinder;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.OpenKinectFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.IplImage;



public class TestSandBox  extends TestCase {

   static Logger logger = Logger.getLogger(TestSandBox.class);
   static {
      BasicConfigurator.configure();
   }

   BufferedImage crop(BufferedImage src, Rectangle rect){
      BufferedImage dest = new BufferedImage((int)rect.getWidth(), (int)rect.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics g = dest.getGraphics();
      g.drawImage(src, 0, 0, (int)rect.getWidth(), (int)rect.getHeight(), (int)rect.getX(), (int)rect.getY(),  (int)rect.getX() +  (int)rect.getWidth(),  (int)rect.getY() + (int)rect.getHeight(), null);
      g.dispose();
      return dest;
   }
   
   
   ///class RawDepthImage extends IplImage {
      
      //IplImage iplImage;
      
      public void writeToFile(IplImage iplImage, File file) throws IOException{         
         ByteBuffer bb = iplImage.getByteBuffer();
         FileChannel wChannel = new FileOutputStream(file).getChannel();
         wChannel.write(bb);
         wChannel.close();
      }
      
      public IplImage readFromFile(File file) throws IOException{
         IplImage imgin = IplImage.create(cvSize(640,480), 16, 1);
         ByteBuffer bbin = imgin.getByteBuffer();         
         FileChannel rChannel = new FileInputStream(file).getChannel();
         rChannel.read(bbin);
         rChannel.close();
         return imgin;
      }
      
  // }
   
   @Test
   public void testSaveDepthImage() throws Exception {

      OpenKinectFrameGrabber fg = new OpenKinectFrameGrabber(0);

      fg.setFormat("depth");
      fg.start();
      IplImage img = fg.grab();
      fg.stop();
      
      writeToFile(img, new File("test.depth"));
      IplImage imgin = readFromFile(new File("test.depth"));
      
      ByteBuffer bb = img.getByteBuffer();
      ByteBuffer bbin = imgin.getByteBuffer();
      
      while (bb.hasRemaining()){
         int a = bb.get();
         int b = bbin.get();
         assertEquals(a,b);         
      }
      
   }
   
   @Test
   public void testKinect() throws InterruptedException {
      
     OpenKinectFrameGrabber fg = new OpenKinectFrameGrabber(0);
     OpenKinectFrameGrabber fg2 = new OpenKinectFrameGrabber(0);
     
     try {
      fg.setFormat("depth");
      fg.start();
      fg2.start();
   } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
     
     IplImage img = null;
   try {
      img = fg.grab();
   } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
     BufferedImage bf = img.getBufferedImage();
     
     
     ImageIcon icon = new ImageIcon(bf);
     icon.setImage(bf);
     JLabel l = new JLabel(icon);
     l.setSize(new Dimension(640,480));

     ImageIcon icon1 = new ImageIcon(bf);
     JLabel l1 = new JLabel(icon1);
     l.setSize(new Dimension(640,480));

     
     JFrame f = new JFrame();
     f.setSize(new Dimension(1400,500));
     f.setLayout(new BoxLayout(f.getContentPane(),BoxLayout.X_AXIS));
     f.add(l);
     f.add(l1);
     f.setVisible(true);
     
     
     IplImage color = null;
     boolean forever = true;
     while (forever){
        
        Thread.sleep(100);
        
        try {
         img = fg.grab();
         color = fg2.grab();
         
         // this is how we get the correct depth value in java
//         ByteBuffer bb = img.getByteBuffer();
//         ShortBuffer b = bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
//         while (b.hasRemaining()){
//            System.out.println(""+b.get());
//         }
         

         
         bf = img.getBufferedImage();         
         icon.setImage(bf);
         icon1.setImage(color.getBufferedImage());
         l.repaint();
         l1.repaint();

      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
        //img.release();        
     }
     
     Object lock = new Object();     
     synchronized(lock){
        lock.wait();
     }
   }
   
   @Test
   public void testComputeMoments() throws IOException, InterruptedException{

      //BufferedImage screen = ImageIO.read(new File("screen.png"));

      //String inputFilename = "chinaccm.png";
      //String inputFilename = "MUSEEDORSAY.png";
      //String inputFilename = "edushi.png";
//      String inputFilename = "findandine.png";
      String inputFilename = "zwavealliance.png";
      
      BufferedImage inputImage = ImageIO.read(new File(inputFilename));
      IplImage result = null;
      result = ImageAnalyzer.computeStructure(inputImage);
      
      CanvasFrame frame = new CanvasFrame(inputFilename);      
      frame.showImage(result);
      
      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }

   }

   @Test
   public void testBackgroundSubtraction() throws IOException, InterruptedException{
      BufferedImage screen = ImageIO.read(new File("screen.png"));

      //IplImage screenImage = cvLoadImage("screen.png");
      IplImage screenImage = cvLoadImage("applestore.png");

      //IplImage screenImage = IplImage.createFrom(screen);
      IplImage screenImageGray = IplImage.create(cvGetSize(screenImage), 8, 1);
      IplImage foregroundMask = IplImage.create(cvGetSize(screenImage), 8, 1);      
      cvCvtColor(screenImage, screenImageGray, CV_RGB2GRAY);

      ImageAnalyzer.computeForegroundMask(screenImageGray, foregroundMask);


      // Objects allocated with a create*() or clone() factory method are automatically released
      // by the garbage collector, but may still be explicitly released by calling release().
      CvMemStorage storage = CvMemStorage.create();

      IplImage input = foregroundMask;
      CvSeq contour = new CvSeq(null);
      cvFindContours(input, storage, contour, Loader.sizeof(CvContour.class),
            CV_RETR_LIST, CV_CHAIN_APPROX_NONE);

      logger.info(contour.elem_size());

      //IplImage result = screenImage.clone();
      IplImage result = IplImage.create(cvGetSize(screenImage), 8, 3);
      //IplImage result = I.copyFrom(arg0).clone();
      //result.copyFrom(screen);
      cvCopy(screenImage,result,foregroundMask);

      while (contour != null && !contour.isNull()) {
         if (contour.elem_size() > 0) {
            //              CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
            //                      storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 0);
            //cvDrawContours(result, contour, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);

            logger.info(contour.elem_size());
         }
         contour = contour.h_next();
      }
      //      


      CanvasFrame frame = new CanvasFrame("Some Title");
      //      frame.showImage(foregroundImage);
      frame.showImage(result);

      logger.info("non zero:" + cvCountNonZero(foregroundMask));

      cvReleaseImage(screenImage);
      cvReleaseImage(screenImageGray);
      cvReleaseImage(foregroundMask);


      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }


   }
   
   
   @Test
   public void testMemoryConsumption() throws IOException, InterruptedException {
      
      //FinderTestImage testImage = new FinderTestImage("fuzzydesktop");
      FinderTestImage testImage = new FinderTestImage("fuzzyfarmville");
      FinderTestTarget testTarget = testImage.getTestTarget(2);
      
      
      BufferedImage inputImage = testImage.getImage();
      BufferedImage targetImage = testTarget.getImage();

      
      TemplateFinder f = new TemplateFinder();
      
//      int k = testTarget.getGroundTruthLocations().size();      

//      for (int i=0;i<1000;++i){
//         //FindResult[] results = f.findTopKSimilarMatches(inputImage, targetImage, 3, 0.80);        
//         FindResult result = f.findTopMatch(inputImage, targetImage);
//      }
      
      while(true){
         FindResult result = f.findFirstMatch(inputImage, targetImage);
      }

      //viewFindResults(inputImage, targetImage, results);
   }
   
   @Test
   public void testFindFuzzy() throws IOException, InterruptedException {
      
      //FinderTestImage testImage = new FinderTestImage("fuzzydesktop");
      FinderTestImage testImage = new FinderTestImage("fuzzyfarmville");
      FinderTestTarget testTarget = testImage.getTestTarget(2);
      
      
      BufferedImage inputImage = testImage.getImage();
      BufferedImage targetImage = testTarget.getImage();

      
      TemplateFinder f = new TemplateFinder();
      
      int k = testTarget.getGroundTruthLocations().size();      
      
      FindResult[] results = f.findSimilarMatches(inputImage, targetImage, 3, 0.80);


      viewFindResults(inputImage, targetImage, results);
   }
//   
//   static void viewFindResults(BufferedImage inputImage, )

   private void viewFindResults(BufferedImage inputImage,
         BufferedImage targetImage, FindResult[] results)
         throws InterruptedException {
      
      FindResultViewer mv = new FindResultViewer(inputImage);
      mv.setVisible(true);
      for (FindResult r : results){
         mv.addMatch(targetImage,r);
      }
      Object lock = new Object();
      synchronized (lock){
         lock.wait();
      }
   }
   
   @Test
   public void testFindAll() throws IOException, InterruptedException {
      //FinderTestImage testImage = new FinderTestImage("sikuliinbox");
      //FinderTestImage testImage = new FinderTestImage("finderfolders");
      //FinderTestImage testImage = new FinderTestImage("macdesktopdark");
      
      //FinderTestImage testImage = new FinderTestImage("xppricingapp");
      FinderTestImage testImage = new FinderTestImage("macdesktopsikuli");
      
//      FinderTestTarget testTarget = testImage.getTestTarget(0);
      FinderTestTarget testTarget = testImage.getTestTarget(5);
//      FinderTestTarget testTarget = testImage.getTestTarget(3);
      
      //ExactColorFinder f = new ExactColorFinder();
      
      BufferedImage inputImage = testImage.getImage();
      BufferedImage targetImage = testTarget.getImage();
      
      
      TemplateFinder f = new TemplateFinder();
      
      int k = testTarget.getGroundTruthLocations().size();      
      
      FindResult[] results = f.findMatches(inputImage, targetImage, k);
      
      
      Set<Point> matchedGroundTruthLocationSet = new HashSet<Point>();
      
      for (FindResult r : results){

         boolean resultMatchesAGroundTruthLocation = false;
         for (Point groundTruthLocation : testTarget.getGroundTruthLocations()){
            if (r.contains(groundTruthLocation)){
               resultMatchesAGroundTruthLocation = true;               
               matchedGroundTruthLocationSet.add(groundTruthLocation);               
            }            
         }

         //assertTrue(resultMatchesAGroundTruthLocation);
         
      }
      
    //  assertEquals(k, matchedGroundTruthLocationSet.size());
            
      viewFindResults(inputImage, targetImage, results);
   }
   
   @Test
   public void testFindTopMatch() throws IOException, InterruptedException{

//      FinderTestImage suite = FinderTestImage.createFromDirectory("sikuliorgbanner");
//      FinderTestInput input = FinderTestInput.createFromDirectory("xpdesktop");
//      FinderTestInput input = FinderTestInput.createFromDirectory("macdesktop");
//      FinderTestInput input = FinderTestInput.createFromDirectory("exactcolor");
//      FinderTestSuite input = FinderTestSuite.createFromDirectory("bubbles");
      
      //FinderTestImage suite = new FinderTestImage("xppricingapp");
      FinderTestImage suite = new FinderTestImage("wherespace");
      
      
      BufferedImage screenImage = suite.getImage();
      
      FindResultViewer mv = new FindResultViewer(screenImage);
      mv.setVisible(true);
      
      for (FinderTestTarget testTarget : suite.getTestTargets()){         
         BufferedImage targetImage = testTarget.getImage();   
         
        logger.debug("test target: " + testTarget.file.getName());
         
           TemplateFinder f = new TemplateFinder();
           //ExactColorFinder f = new ExactColorFinder();
        
            FindResult r = f.findFirstMatch(screenImage, targetImage);
         
            mv.addMatch(targetImage,r);

            Point gt = testTarget.getGroundTruthLocation();
         //   assertThat(r, foundAtGroundTruthLocation(gt));
         
        // }
         
         logger.info("passed");

      }
      
      Object lock = new Object();
      synchronized (lock){
         lock.wait();
      }
      
   }


   @Test
   public void testRandomSubimage() throws IOException{

      // Given an input image, sample a subimage and templatefinder should
      // find that subimage at the same location, unless there're duplicates

      BufferedImage screen = ImageIO.read(new File("screen.png"));

      int dy = 50;
      int tw = 100;
      int th = 100;
      for (int ty=0; ty < screen.getHeight()-2*dy - th ; ty += dy){
         BufferedImage target = crop(screen, new Rectangle(20,ty,tw,th));
         FindResult r = BaseTemplateFinder.findGroundTruthTopMatch(screen, target, 0.95);
         Logger.getRootLogger().info(r);
      }



   }

   @Factory
   static <T> Matcher<FindResult> samePlaceAs(FindResult other) {
      return new SamePlaceAs(other);
   }
   
   @Factory
   static <T> Matcher<FindResult> foundAtGroundTruthLocation(Point location) {
      return new FoundAtGroundTruthLocation(location);
   }


}

class FoundAtGroundTruthLocation extends TypeSafeMatcher<FindResult> {

   Point groundTruthLocation;
   FoundAtGroundTruthLocation(Point groundTruthLocation){
      this.groundTruthLocation = groundTruthLocation;
   }

   @Override
   public boolean matchesSafely(FindResult self) {

      Rectangle r = new Rectangle(self.x,self.y,self.width,self.height);
      
      boolean resultContainsGroundTruthLocation = r.contains(groundTruthLocation);      
      return resultContainsGroundTruthLocation;      
   }

   @Override
   public void describeTo(Description description) {
      description.appendText("find result should be at location: " + groundTruthLocation);
   }

}

class SamePlaceAs extends TypeSafeMatcher<FindResult> {

   FindResult other;
   SamePlaceAs(FindResult other){
      this.other = other;
   }

   @Override
   public boolean matchesSafely(FindResult self) {

      return (self.x == other.x && 
            self.y == other.y && 
            self.width == other.width && 
            self.height == other.height);
      //      return false;
   }

   @Override
   public void describeTo(Description description) {
      description.appendText("find result should be at the same place as: " + other);
   }

}

//class FinderTestImage {
//   File file;
//   BufferedImage image;
//   BufferedImage getImage() throws IOException{
//      if (image == null){
//         image = ImageIO.read(file);
//      }
//      return image;
//   }
//
//}