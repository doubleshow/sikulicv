package org.sikuli.cv;

import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.sikuli.cv.FindResult;
import org.sikuli.cv.GUITargetFinder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FindTopMatchTestCase extends TestCase {

   FinderTestTarget target = null;
   FindTopMatchTestCase(FinderTestTarget target){
      this.target = target;
   }
   
   @Override
   public String getName(){
      return "testFindTopMatch [" + target.file + "]";
   }

   @Factory
   static <T> Matcher<FindResult> foundAtGroundTruthLocation(Point location) {
      return new FoundAtGroundTruthLocation(location);
   }

   
   @Override   
   public void runTest() throws IOException {
      BufferedImage screenImage = target.getScreenImage();
      BufferedImage targetImage = target.getImage();   

      GUITargetFinder f = new GUITargetFinder();

      FindResult r = f.findTopMatch(screenImage, targetImage);

      Point gt = target.getGroundTruthLocation();
      assertThat(r, foundAtGroundTruthLocation(gt));
   }

   
   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "Test Finder";
         }
      };
      
      
    String[] names = new String[] {"sikuliorgbanner","xpdesktop","macdesktop","xppricingapp"};
    
    for (String name : names){

       FinderTestImage findertestsuite = new FinderTestImage(name);

       int n = findertestsuite.getTestCases().size();
       for (int i=0; i < n; ++i){

          FinderTestTarget ts = findertestsuite.getTestTarget(i);        
          TestCase tc = new FindTopMatchTestCase(ts);
          suite.addTest(tc);
       }
     
    }
    
    names = new String[] {"macdesktopdark","xpfolders","sikuliinbox","finderfolders"};
    
//    for (String name : names){
//
//       FinderTestImage testImage = new FinderTestImage(name);
//
//       int n = testImage.getTestCases().size();
//       for (int i=0; i < n; ++i){
//
//          FinderTestTarget t = testImage.getTestTarget(i);        
//          TestCase test = new FindTopKMatchesTestCase(t);
//          suite.addTest(test);        
//       }
//     
//    }
//      
      return (Test) suite;
   }

}


class FindTopKMatchesTestCase extends TestCase {

   FinderTestTarget target = null;
   FindTopKMatchesTestCase(FinderTestTarget target){
      this.target = target;
   }
   
   @Override
   public String getName(){
      return "testFindTopKMatches [" + target.getTestImage() + ":" +  target + "]";
   }

   @Override   
   public void runTest() {
      BufferedImage inputImage = null;
      BufferedImage targetImage = null;
      try {
         inputImage = target.getScreenImage();
         targetImage = target.getImage();
      } catch (IOException e) {
         e.printStackTrace();
      }
      
      GUITargetFinder f = new GUITargetFinder();
      
      int k = target.getGroundTruthLocations().size();      
      
      FindResult[] results = f.findTopKMatches(inputImage, targetImage, k);
      
      
      Set<Point> matchedGroundTruthLocationSet = new HashSet<Point>();
      
      for (FindResult r : results){

         boolean resultMatchesAGroundTruthLocation = false;
         for (Point groundTruthLocation : target.getGroundTruthLocations()){
            if (r.contains(groundTruthLocation)){
               resultMatchesAGroundTruthLocation = true;               
               matchedGroundTruthLocationSet.add(groundTruthLocation);               
            }            
         }
         
         assertTrue(resultMatchesAGroundTruthLocation);
         
      }
      
      assertEquals(k, matchedGroundTruthLocationSet.size());
   }
}