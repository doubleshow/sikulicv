package org.sikuli.cv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.sikuli.cv.GUITargetFinder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class FinderTestSuite extends TestCase {

   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "Test Finder";
         }
      };

      suite.addTestSuite(DenerativeTestCase.class);
      suite.addTest(FindFirstMatchTestCase.suite());
      suite.addTest(FindMatchesTestCase.suite());
      suite.addTest(FindSimilarMatchesTestCase.suite());
      suite.addTest(FindGoodMatchTestCase.suite());
      
      return (Test) suite;
   }

   static public class DenerativeTestCase extends TestCase {
      
      public DenerativeTestCase(){
         super("Denerative Cases");
      }
      

      File root = new File("src/test/resources/degenerative");
      
      public void testTargetIsLargerThanInputImage() throws IOException{
         
         BufferedImage smallInput = ImageIO.read(new File(root,"smallInput.png"));
         BufferedImage largeTarget = ImageIO.read(new File(root, "largeTarget.png"));
         
         GUITargetFinder f = new GUITargetFinder();
         FindResult r = f.findFirstMatch(smallInput, largeTarget);
         assertThat(r, equalTo(null));
         
         FindResult[] rs = f.findSimilarMatches(smallInput, largeTarget, 10, 0.95);
         assertThat(rs.length, equalTo(0));

         rs = f.findMatches(smallInput, largeTarget, 10);
         assertThat(rs.length, equalTo(0));
         
         r = f.findFirstGoodMatch(smallInput, largeTarget);
         assertThat(r, equalTo(null));
         
      }
      
      public void testInputAndTargetAreTheSame() throws IOException{         
         BufferedImage smallInput = ImageIO.read(new File(root,"smallInput.png"));
         
         GUITargetFinder f = new GUITargetFinder();
         FindResult r = f.findFirstMatch(smallInput, smallInput);
         assertThat(r.x, equalTo(0));
         assertThat(r.y, equalTo(0));
         assertThat(r.width, equalTo(smallInput.getWidth()));
         assertThat(r.height, equalTo(smallInput.getHeight()));
      }
      
      public void testNegativeSimilarityValues() throws IOException{         
         BufferedImage input = ImageIO.read(new File(root,"screen.png"));
         BufferedImage target = ImageIO.read(new File(root,"target.png"));
         
         GUITargetFinder f = new GUITargetFinder();
         FindResult[] rs = f.findSimilarMatches(input, target, 10, -1.0);
         assertThat(rs.length, equalTo(10));
      }
      
      public void testSimilarityValueGreaterThanOrEqualToOne() throws IOException{         
         BufferedImage input = ImageIO.read(new File(root,"screen.png"));
         BufferedImage target = ImageIO.read(new File(root,"target.png"));
         
         GUITargetFinder f = new GUITargetFinder();
         FindResult[] rs = f.findSimilarMatches(input, target, 10, 1.5);
         assertThat(rs.length, equalTo(1));

         rs = f.findSimilarMatches(input, target, 10, 1.0);
         assertThat(rs.length, equalTo(1));

      }
      
      public void testReallyLargeKDoesNotTakeForever() throws IOException{         
         BufferedImage input = ImageIO.read(new File(root,"screen.png"));
         BufferedImage target1 = ImageIO.read(new File(root,"target1.png"));
         
         GUITargetFinder f = new GUITargetFinder();
         FindResult[] rs = f.findSimilarMatches(input, target1, 1000, 0.5);
         assertThat(rs.length, equalTo(1000));

      }

   }
}

class FindFirstMatchTestCase extends TestCase {
   
   static String[] names = new String[] {"sikuliorgbanner","xpdesktop","macdesktop","xppricingapp","wherespace"};   
   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "findFirstMatch";
         }
      };

      for (String name : names){

         FinderTestImage image = new FinderTestImage(name);
         int n = image.getTestTargets().size();
         for (int i=0; i < n; ++i){
            FinderTestTarget ts = image.getTestTarget(i);        
            TestCase tc = new FindFirstMatchTestCase(ts);
            suite.addTest(tc);
         }       
      }
      return suite;
   }

   FinderTestTarget target = null;
   FindFirstMatchTestCase(FinderTestTarget target){
      this.target = target;
   }

   @Override
   public String getName(){
      return "[" + target.getTestImage() + "<->" + target + "]";
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

      FindResult r = f.findFirstMatch(screenImage, targetImage);

      Point gt = target.getGroundTruthLocation();
      assertThat(r, foundAtGroundTruthLocation(gt));
   }
   

}

class FindMatchesTestCase extends TestCase {
   
   static String[] names = new String[] {"macdesktopsikuli","macdesktopdark","xpfolders","sikuliinbox","finderfolders"};
   public static Test suite(){      
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "findMatches";
         }
      };

      for (String name : names){

         FinderTestImage testImage = new FinderTestImage(name);

         int n = testImage.getTestTargets().size();
         for (int i=0; i < n; ++i){

            FinderTestTarget t = testImage.getTestTarget(i);        
            TestCase test = new FindMatchesTestCase(t);
            suite.addTest(test);        
         }

      }
      
      return suite;
   }
   
   
   FinderTestTarget target = null;
   FindMatchesTestCase(FinderTestTarget target){
      this.target = target;
   }

   @Override
   public String getName(){
      return "[" + target.getTestImage() + "<->" +  target + "]";
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

      FindResult[] results = f.findMatches(inputImage, targetImage, k);


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

class FindGoodMatchTestCase extends TestCase {


   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "findFirstGoodMatch";
         }
      };
      
      String mac = "macdesktop";
      String xp = "xpdesktop";
      
      FinderTestImage macTestImage = new FinderTestImage(mac);
      FinderTestImage xpTestImage = new FinderTestImage(xp);

      for (FinderTestTarget macTarget : macTestImage.getTestTargets()){
            TestCase t = FindGoodMatchTestCase.createHasNoGoodMatchTestCase(macTarget,xpTestImage);
            suite.addTest(t);
      }
      
      for (FinderTestTarget macTarget : macTestImage.getTestTargets()){
         TestCase t = FindGoodMatchTestCase.createHasAGoodMatchTestCase(macTarget,macTestImage);
         suite.addTest(t);
   }
      
      return suite;
   }

   
   FinderTestImage testImage = null;
   FinderTestTarget target = null;
   boolean expectedExistence = false;
   
   static FindGoodMatchTestCase createHasAGoodMatchTestCase(FinderTestTarget target, FinderTestImage testImage){
      return new FindGoodMatchTestCase(target, testImage, true);
   }
   
   static FindGoodMatchTestCase createHasNoGoodMatchTestCase(FinderTestTarget target, FinderTestImage testImage){
      return new FindGoodMatchTestCase(target, testImage, false);
   }

   
   FindGoodMatchTestCase(FinderTestTarget target, FinderTestImage testImage, boolean expectedExistence){
      this.testImage = testImage;
      this.target = target;
      this.expectedExistence = expectedExistence;
   }

   @Override
   public String getName(){
      String prefix;
      if (expectedExistence){
         prefix = "testHasAGoodMatch";
      }else{
         prefix = "testHasNoGoodMatch";
      }
      return prefix + "[" + testImage + " <-> " + target.getTestImage() + " : " + target + "]";
   }
   
   @Override   
   public void runTest() {
      GUITargetFinder f = new GUITargetFinder();
      try {
         FindResult r = f.findFirstGoodMatch(testImage.getImage(), target.getImage());
         if (!expectedExistence){
            assertThat(r, equalTo(null));
         }else{
            assertThat(r, not(equalTo(null)));
         }
      } catch (IOException e) {
         e.printStackTrace();
      }      
      
   }

}

class FindSimilarMatchesTestCase extends TestCase {

   static String[] names = new String[] {"fuzzydesktop","fuzzyfarmville"};
   public static Test suite(){
      TestSuite suite = new TestSuite(){
         @Override
         public String getName(){
            return "findSimilarMatches";
         }
      };
      for (String name : names){

         FinderTestImage findertestsuite = new FinderTestImage(name);

         int n = findertestsuite.getTestTargets().size();
         for (int i=0; i < n; ++i){

            FinderTestTarget ts = findertestsuite.getTestTarget(i);        
            TestCase tc = new FindSimilarMatchesTestCase(ts);
            suite.addTest(tc);
         }       
      }
      return suite;
   }

   FinderTestTarget target = null;
   FindSimilarMatchesTestCase(FinderTestTarget target){
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

      FindResult[] results = f.findSimilarMatches(inputImage, targetImage, k+10, minSimilarity);

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
      
      results = f.findSimilarMatches(inputImage, targetImage, k+10, minSimilarity);

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

