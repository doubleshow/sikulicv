package org.sikuli.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JWindow;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.sikuli.cv.FindResult;
import org.sikuli.cv.GUITargetFinder;
import org.sikuli.cv.TemplateFinder;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TemplateFinderTest  extends TestCase {

   static Logger logger = Logger.getLogger(TemplateFinderTest.class);
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

   
   CvScalar green = cvScalar(0,255,0,255);
   CvScalar blue = cvScalar(255,0,0,255);
   CvScalar red = cvScalar(0,0,255,255);
  
   
   
   private void computeWeightsForHalvesRelativeToPoint(IplImage foregroundMask, IplImage resultImage, 
         CvPoint origin, CvScalar color){
      
      int w = foregroundMask.width();
      int h = foregroundMask.height();
      int ix = origin.x();
      int iy = origin.y();

      CvRect upperRect = cvRect(0,0,w,iy);
      CvRect lowerRect = cvRect(0,iy,w,h-iy);
      CvRect leftRect = cvRect(0,0,ix,h);
      CvRect rightRect = cvRect(ix+1,0,w-ix,h);
      
      cvSetImageROI(foregroundMask, upperRect);      
      int upperWeight = cvCountNonZero(foregroundMask);
      
      cvSetImageROI(foregroundMask, lowerRect);
      int lowerWeight = cvCountNonZero(foregroundMask);

      cvSetImageROI(foregroundMask, leftRect);
      int leftWeight = cvCountNonZero(foregroundMask);
      
      cvSetImageROI(foregroundMask, rightRect);
      int rightWeight = cvCountNonZero(foregroundMask);

      logger.info("upper weight:" + upperWeight);
      logger.info("lower weight:" + lowerWeight);
      logger.info("left weight:" + leftWeight);
      logger.info("right weight:" + rightWeight);
      
      double verticalBalance = Math.abs(1.0*upperWeight - lowerWeight) / Math.max(upperWeight,lowerWeight);    
      logger.info("verticalBalance:" + verticalBalance);

      double horizontalBalance = Math.abs(1.0*leftWeight - rightWeight) / Math.max(leftWeight,rightWeight);    
      logger.info("horizontalBalance:" + horizontalBalance);

      
      CvFont font = new CvFont(CV_FONT_HERSHEY_SIMPLEX, 1.0, 1.0, 0, 2, CV_AA);      


      cvPutText(resultImage, ""+upperWeight, cvPoint(ix-50,iy-60), font, color);
      cvPutText(resultImage, ""+lowerWeight, cvPoint(ix-50,iy+60), font, color);
      cvPutText(resultImage, ""+leftWeight, cvPoint(ix-150,iy), font, color);
      cvPutText(resultImage, ""+rightWeight, cvPoint(ix+10,iy), font, color);   

      cvPutText(resultImage, "("+String.format("%.2f", 1-horizontalBalance)+")", cvPoint(ix+150,iy), font, color);
      cvPutText(resultImage, "("+String.format("%.2f", 1-verticalBalance)+")", cvPoint(ix-50,iy+100), font, color);
      
      cvResetImageROI(foregroundMask);
   }
   
//   private void computeWeightsForHalvesOfImage(IplImage foregroundMask, IplImage resultImage){
//      int w = foregroundMask.width();
//      int h = foregroundMask.height();
//      int ix = w/2;
//      int iy = h/2;      
//      
//      CvRect upperRect = cvRect(0,0,w,h/2);
//      CvRect lowerRect = cvRect(0,h/2,w,h/2);
//      CvRect leftRect = cvRect(0,0,w/2,h);
//      CvRect rightRect = cvRect(w/2,0,w/2,h);
//      
//      cvSetImageROI(foregroundMask, upperRect);      
//      int upperWeight = cvCountNonZero(foregroundMask);
//      
//      cvSetImageROI(foregroundMask, lowerRect);
//      int lowerWeight = cvCountNonZero(foregroundMask);
//
//      cvSetImageROI(foregroundMask, leftRect);
//      int leftWeight = cvCountNonZero(foregroundMask);
//      
//      cvSetImageROI(foregroundMask, rightRect);
//      int rightWeight = cvCountNonZero(foregroundMask);
//
//      logger.info("upper weight:" + upperWeight);
//      logger.info("lower weight:" + lowerWeight);
//      logger.info("left weight:" + leftWeight);
//      logger.info("right weight:" + rightWeight);
//      
//      double verticalBalance = (1.0*upperWeight - lowerWeight) / Math.max(upperWeight,lowerWeight);    
//      logger.info("verticalBalance:" + verticalBalance);
//      
//      CvFont font = new CvFont(CV_FONT_HERSHEY_SIMPLEX, 1.0, 0.5, 0, 1, CV_AA);      
//
//      cvPutText(resultImage, ""+upperWeight, cvPoint(ix-50,iy-60), font, red);
//      cvPutText(resultImage, ""+lowerWeight, cvPoint(ix-50,iy+60), font, red);
//      cvPutText(resultImage, ""+leftWeight, cvPoint(ix-100,iy), font, red);
//      cvPutText(resultImage, ""+rightWeight, cvPoint(ix+10,iy), font, red);      
//   }
   
   
   @Test
   public void testComputeMoments() throws IOException, InterruptedException{

      //BufferedImage screen = ImageIO.read(new File("screen.png"));

      //String inputFilename = "chinaccm.png";
      //String inputFilename = "MUSEEDORSAY.png";
      //String inputFilename = "edushi.png";
//      String inputFilename = "findandine.png";
      String inputFilename = "zwavealliance.png";

      IplImage screenImage = cvLoadImage(inputFilename);

//      IplImage screenImage = IplImage.createFrom(screen);
      IplImage screenImageGray = IplImage.create(cvGetSize(screenImage), 8, 1);
      IplImage foregroundMask = IplImage.create(cvGetSize(screenImage), 8, 1);      
      cvCvtColor(screenImage, screenImageGray, CV_RGB2GRAY);

      ImageAnalyzer.computeForegroundMask(screenImageGray, foregroundMask);
      
      
      IplImage result = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvCopy(screenImage,result,foregroundMask);
      
      
      
      
      CvMoments moments = new CvMoments();
      cvMoments(foregroundMask, moments, 1);
      logger.info("m00:" + moments.m00());
      logger.info("m01:" + moments.m01());
      logger.info("m10:" + moments.m10());      
      logger.info("cy:" + moments.m01()/moments.m00());
      logger.info("cx:" + moments.m10()/moments.m00());
      
      
      // center of mass of foreground pixels
      int cy = (int) (moments.m01()/moments.m00());
      int cx = (int) (moments.m10()/moments.m00());
      CvPoint massCenter = cvPoint(cx, cy);
      
      int w = screenImage.width();
      int h = screenImage.height();
      
      // image center
      int ix = screenImage.width()/2;
      int iy = screenImage.height()/2;      
      CvPoint imageCenter = cvPoint(ix,iy);
      
      
      //cvBoundingRect()
      IplImage flippedForegroundImage = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvCopy(screenImage,flippedForegroundImage,foregroundMask);

      IplImage flippedForegroundMask = foregroundMask.clone();
      
      CvRect foregroundRect = cvRect(0,0,cx+cx,h);
      cvSetImageROI(flippedForegroundImage, foregroundRect);
      cvSetImageROI(flippedForegroundMask, foregroundRect);
            
      cvFlip(flippedForegroundImage, null, 1); // flip y-axis
      cvFlip(flippedForegroundMask, null, 1);
      
      cvResetImageROI(flippedForegroundImage);
      cvResetImageROI(flippedForegroundMask);
      
      IplImage symmetryMatchMask = IplImage.create(cvGetSize(screenImage), 8, 1);
      cvAnd(foregroundMask,flippedForegroundMask,symmetryMatchMask, null);
      
      IplImage symmetryMatchColorMask = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvSet(symmetryMatchColorMask, red, symmetryMatchMask);
      
      
      // mixing
      cvAddWeighted(result, 0.7, flippedForegroundImage, 0.3, 0, result);
      cvAddWeighted(result, 0.5, symmetryMatchColorMask, 0.5, 0, result);
      
      cvCircle(result, massCenter, 20, green, 1,8,0);
      cvCircle(result, imageCenter, 20, blue, 1,8,0);
            
      // central vertical axis      
      cvLine(result, cvPoint(cx,0), cvPoint(cx,h), green, 2,8,0);
      
      // central horizontal axis      
      cvLine(result, cvPoint(0,cy), cvPoint(w,cy), green, 2,8,0);
      
      // central vertical axis      
      cvLine(result, cvPoint(ix,0), cvPoint(ix,h), blue, 2,8,0);
      
      // central horizontal axis      
      cvLine(result, cvPoint(0,iy), cvPoint(w,iy), blue, 2,8,0);

      computeWeightsForHalvesRelativeToPoint(foregroundMask, result, imageCenter, blue);
      computeWeightsForHalvesRelativeToPoint(foregroundMask, result, massCenter, green);
      
//      IplImage flippedForegroundMask = foregroundMask.clone();      
//      cvFlip(foregroundMask, flippedForegroundMask, 0);
      
//      IplImage flippedForegroundImage = IplImage.create(cvGetSize(screenImage), 8, 3);
//      cvCopy(screenImage,flippedForegroundImage,foregroundMask);
//      cvFlip(flippedForegroundImage, null, 1);

      
      cvSaveImage(""+inputFilename + "_analyzed.png", result);
      //cvAddWeighted(result, 0.8, flippedForegroundImage, 0.2, 0, result);
      

      CanvasFrame frame = new CanvasFrame(inputFilename);      
//      frame.showImage(flippedForegroundImage);
      //frame.showImage(flippedForegroundImage);
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
   public void testGTLabler() throws IOException, InterruptedException {
//    FinderTestInput input = FinderTestInput.createFromDirectory("exactcolor");
//      FinderTestSuite input = FinderTestSuite.createFromDirectory("bubbles");      
//      FinderTestSuite input = FinderTestSuite.createFromDirectory("sikuliorgbanner");
      //FinderTestImage input = FinderTestImage.createFromDirectory("macdesktop");
      //FinderTestSuite input = FinderTestSuite.createFromDirectory("xpdesktop")
      
      //FinderTestImage image = new FinderTestImage("sikuliinbox");
      //FinderTestImage image = new FinderTestImage("finderFolders");
      //FinderTestImage image = new FinderTestImage("xpfolders");
      //FinderTestImage image = new FinderTestImage("macdesktopdark");
      //FinderTestImage image = new FinderTestImage("xppricingapp");
      FinderTestImage image = new FinderTestImage("wherespace");
      
      FinderTestImageEditor gt = new FinderTestImageEditor(image);
      gt.setVisible(true);
      
      
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
      
      FinderTestImage testImage = new FinderTestImage("xppricingapp");
      
//      FinderTestTarget testTarget = testImage.getTestTarget(0);
      FinderTestTarget testTarget = testImage.getTestTarget(0);
//      FinderTestTarget testTarget = testImage.getTestTarget(3);
      
      //ExactColorFinder f = new ExactColorFinder();
      
      BufferedImage inputImage = testImage.getImage();
      BufferedImage targetImage = testTarget.getImage();
      
      FindResultViewer mv = new FindResultViewer(inputImage);
      mv.setVisible(true);
      
      GUITargetFinder f = new GUITargetFinder();
      
      int k = testTarget.getGroundTruthLocations().size() + 2;      
      
      FindResult[] results = f.findTopKMatches(inputImage, targetImage, k);
      
      
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
            
      for (FindResult r : results){
         mv.addMatch(targetImage,r);
      }
      
      
      

      Object lock = new Object();
      synchronized (lock){
         lock.wait();
      }
   }
   
   @Test
   public void testFindTopMatch() throws IOException, InterruptedException{

//      FinderTestImage suite = FinderTestImage.createFromDirectory("sikuliorgbanner");
//      FinderTestInput input = FinderTestInput.createFromDirectory("xpdesktop");
//      FinderTestInput input = FinderTestInput.createFromDirectory("macdesktop");
//      FinderTestInput input = FinderTestInput.createFromDirectory("exactcolor");
//      FinderTestSuite input = FinderTestSuite.createFromDirectory("bubbles");
      
      FinderTestImage suite = new FinderTestImage("xppricingapp");
      
      
      BufferedImage screenImage = suite.getImage();
      
      FindResultViewer mv = new FindResultViewer(screenImage);
      mv.setVisible(true);
      
      for (FinderTestTarget testTarget : suite.getTestCases()){         
         BufferedImage targetImage = testTarget.getImage();   
         
        logger.debug("test target: " + testTarget.file.getName());
         
           GUITargetFinder f = new GUITargetFinder();
           //ExactColorFinder f = new ExactColorFinder();
        
            FindResult r = f.findTopMatch(screenImage, targetImage);
         
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
         FindResult r = TemplateFinder.findGroundTruthTopMatch(screen, target, 0.95);
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