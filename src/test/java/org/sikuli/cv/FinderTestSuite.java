package org.sikuli.cv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.sikuli.cv.GUITargetFinder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FinderTestSuite extends TestCase {

   private static void addFindTopKMatchesTestCases(TestSuite suite){
      String[] names = new String[] {"macdesktopsikuli","macdesktopdark","xpfolders","sikuliinbox","finderfolders"};

      for (String name : names){

         FinderTestImage testImage = new FinderTestImage(name);

         int n = testImage.getTestCases().size();
         for (int i=0; i < n; ++i){

            FinderTestTarget t = testImage.getTestTarget(i);        
            TestCase test = new FindTopKMatchesTestCase(t);
            suite.addTest(test);        
         }

      }
   }

   private static void addFindTopMatchTestCases(TestSuite suite){
      String[] names = new String[] {"sikuliorgbanner","xpdesktop","macdesktop","xppricingapp","wherespace"};

      for (String name : names){

         FinderTestImage findertestsuite = new FinderTestImage(name);

         int n = findertestsuite.getTestCases().size();
         for (int i=0; i < n; ++i){

            FinderTestTarget ts = findertestsuite.getTestTarget(i);        
            TestCase tc = new FindTopMatchTestCase(ts);
            suite.addTest(tc);
         }       
      }
   }
   
   private static void addFindTopKSimilarMatchesTestCases(TestSuite suite){
      String[] names = new String[] {"fuzzydesktop","fuzzyfarmville"};

      for (String name : names){

         FinderTestImage findertestsuite = new FinderTestImage(name);

         int n = findertestsuite.getTestCases().size();
         for (int i=0; i < n; ++i){

            FinderTestTarget ts = findertestsuite.getTestTarget(i);        
            TestCase tc = new FindTopKSimilarMatchesTestCase(ts);
            suite.addTest(tc);
         }       
      }
   }

   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "Test Finder";
         }
      };


      addFindTopMatchTestCases(suite);
      addFindTopKMatchesTestCases(suite);
      addFindTopKSimilarMatchesTestCases(suite);

      return (Test) suite;
   }

}

class FindTopMatchTestCase extends TestCase {

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

class FindTopKSimilarMatchesTestCase extends TestCase {

   FinderTestTarget target = null;
   FindTopKSimilarMatchesTestCase(FinderTestTarget target){
      this.target = target;
   }

   @Override
   public String getName(){
      return "testFindTopKSimilarMatches [" + target.getTestImage() + ":" +  target + "]";
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
      
      // test with a very low similarity threshold
      
      float minSimilarity = 0.5f;

      FindResult[] results = f.findTopKSimilarMatches(inputImage, targetImage, k+10, minSimilarity);

      // check whether the desired number of matches were returned,
      // since the threshold is low, we expect to get all the matches we wanted     
      assertEquals(results.length, k+10);

      Set<Point> matchedGroundTruthLocationSet = new HashSet<Point>();

      // check whether the top K results correspond to the ground truth results
      for (int i = 0; i < k; ++i){

         FindResult r = results[i];
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
      
      // check that whether every result has a similarity score above the threshold
      for (FindResult r : results){
         assertTrue(r.score >= minSimilarity);
      }
      
      
      // test with a more strict similarity threshold
      minSimilarity = 0.95f;
      
      results = f.findTopKSimilarMatches(inputImage, targetImage, k+10, minSimilarity);

      // check whether the number of results returned does not exceed what was requested,
      // since the similarity threshold is high, we may not necessarily get all the
      // matches we requested
      assertThat(results.length, lessThanOrEqualTo(k+10));
      
      // check that whether every result has a similarity score above the threshold
      for (FindResult r : results){
         assertTrue(r.score >= minSimilarity);
      }

      // check whether the top results we did receive were the ground truth results
      matchedGroundTruthLocationSet = new HashSet<Point>();
      for (int i = 0; i < Math.min(results.length, k); ++i){

         FindResult r = results[i];
         boolean resultMatchesAGroundTruthLocation = false;
         for (Point groundTruthLocation : target.getGroundTruthLocations()){
            if (r.contains(groundTruthLocation)){
               resultMatchesAGroundTruthLocation = true;               
               matchedGroundTruthLocationSet.add(groundTruthLocation);               
            }            
         }
         assertTrue(resultMatchesAGroundTruthLocation);
      }
      
      
   }
}