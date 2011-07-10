package org.sikuli.cv;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
 
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
 
/**
 * JUnit Parameterized Test
 * @author mkyong
 *
 */
@RunWith(value = Parameterized.class)
public class JunitTest6 {
 
    private int number;
    FinderTestTarget testCase;
 
    public JunitTest6(FinderTestTarget testCase){
       this.testCase = testCase;
    }
 
    @Parameters
    public static Collection<Object[]> data() {
       
       FinderTestImage suite = FinderTestImage.createFromDirectory("sikuliorgbanner");
//     FinderTestInput input = FinderTestInput.createFromDirectory("xpdesktop");
//     FinderTestInput input = FinderTestInput.createFromDirectory("macdesktop");
//     FinderTestInput input = FinderTestInput.createFromDirectory("exactcolor");
//     FinderTestSuite input = FinderTestSuite.createFromDirectory("bubbles");      
//     BufferedImage screenImage = suite.getImage();
//     
//     MatchViewer mv = new MatchViewer(screenImage);
//     mv.setVisible(true);
     
//     for (FinderTestCase testTarget : suite.getTestCases()){         
//        BufferedImage targetImage = testTarget.getImage();   
//        
//        logger.debug("test target: " + testTarget.file.getName());
//        
//          GUITargetFinder f = new GUITargetFinder();
//          //ExactColorFinder f = new ExactColorFinder();
//       
//           FindResult r = f.findTopMatch(screenImage, targetImage);
//        
//           mv.addMatch(targetImage,r);
//
//           Point gt = testTarget.getGroundTruthLocation();
//           assertThat(r, foundAtGroundTruthLocation(gt));
//        
//       // }
//        
//        logger.info("passed");
//
//     }
     
     int n = suite.getTestCases().size();
     
     
       
      Object[][] data = new Object[n][1];
      
      for (int i=0; i < n; ++i){
         
         data[i][0] = suite.getTestTarget(i);
      }
      
      return Arrays.asList(data);
    }
    
    public String getName(){
       return ""+testCase.file;
    }
 
    @Test
    public void pushTest() {
      System.out.println("Parameterized Number is : " + number +testCase.file);
      if ((int)number == 3){
         //assert(false);
         // assertEquals(1,2);

      }
    }
 
 
}