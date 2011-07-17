package org.sikuli.kinect;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JComponent;


import org.fest.swing.core.ComponentDragAndDrop;
import org.fest.swing.core.GenericTypeMatcher;
import org.fest.swing.core.MouseButton;
import org.fest.swing.core.MouseClickInfo;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JButtonFixture;
import org.fest.swing.fixture.JLabelFixture;
import org.fest.swing.fixture.JListFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JTextComponentFixture;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class KinectMeasureTest {
   
   
   private KinectMeasureMainFrame main;
   private MeasurementFile file;
   private Measurement firstMeasurement;
   private File bundlePath = new File("testSave.kmx");
   
   private FrameFixture window;
   //private PanelFixture captureView;
   private JPanelFixture captureView;
   private JPanelFixture measureView;
   private JListFixture listView;
   private JButtonFixture captureButton;
   
   static public class EmptyStartupTest {
      
      private KinectMeasureMainFrame main;
      private FrameFixture window;
      private JButtonFixture captureButton;
      private JPanelFixture captureView;
      private JPanelFixture measureView;
      private JListFixture listView;
      
      @Before public void setUp() throws Exception{
         
         main = GuiActionRunner.execute(new GuiQuery<KinectMeasureMainFrame>() {
             protected KinectMeasureMainFrame executeInEDT() {
               return new KinectMeasureMainFrame();  
             }
         });
         
         main.setDualFrameGrabber(new FakeKinectDualFrameGrabber());
         
         window = new FrameFixture(main);
         window.show(); // shows the frame to test
         
//         listView = window.list("MeasurementFileListView");
//         measureView = window.panel("MeasurementFrameView");
         captureButton = window.button("Capture");
      }
      
      @Test
      public void testCaptureFirstFrame(){         
         captureButton.click();   
         captureView = window.panel("CaptureView");
         captureView.doubleClick();
         
         assertThat(main.getMeasurementFile().getMeasurementFrames().size(), equalTo(1));
      }

      @Test
      public void testManually() throws InterruptedException{         
         //captureButton.click();   
         //window.menuItem("Save").click();
         window.menuItem("Open").click();
         Object lock = new Object();
         synchronized(lock){
            lock.wait();
         }
      }

   }
   
   
   @Before
   public void setUp() throws Exception{
      
      main = GuiActionRunner.execute(new GuiQuery<KinectMeasureMainFrame>() {
          protected KinectMeasureMainFrame executeInEDT() {
            return new KinectMeasureMainFrame();  
          }
      });
      
      main.loadFromBundlePath(bundlePath);
      main.setDualFrameGrabber(new FakeKinectDualFrameGrabber());
      
      file = main.getMeasurementFile();
      
      firstMeasurement = file.getMeasurementFrames().get(0).getMeasurements().get(0);
      
      window = new FrameFixture(main);
      window.show(); // shows the frame to test
      
      listView = window.list("MeasurementFileListView");
      measureView = window.panel("MeasurementFrameView");
      captureButton = window.button("Capture");
   }   
   
   @Test
   public void testManually() throws InterruptedException{
      //main.doCapture();
      
      Object lock = new Object();
      synchronized(lock){
         lock.wait();
      }
   }
   
   @Test
   public void testDoubleClickLineToLabelGroundTruth() {

      JLabelFixture label = measureView.label("GroundTruthLabel_50_50");      
      label.doubleClick();

      JTextComponentFixture textBox = measureView.textBox("GroundTruthEditor_50_50");
      textBox.requireText(""+firstMeasurement.groundTruth);
      textBox.focus();
      
      textBox.enterText("120");
      textBox.pressAndReleaseKeys(KeyEvent.VK_ENTER);//measureView.click();
      textBox.requireNotVisible();
      
      label.requireText("120");
      
      assertThat(firstMeasurement.groundTruth, equalTo(120));
      
   }
      
   
   @Test
   public void testDoCaptureNewFrame() throws InterruptedException{
      //main.doCapture();

      captureButton.click();
      
      captureButton.requireDisabled();
      
      
      int n = file.getMeasurementFrames().size();
      
      captureView = window.panel("CaptureView");
      captureView.doubleClick();
      
      // the file should have an additional frame
      assertThat(file.getMeasurementFrames().size(), equalTo(n+1));
      
      // the list view should be updated too
      listView.requireItemCount(n+1);
      
      // the newly added frame (last one) should be selected
      listView.requireSelection(n);
      
      captureButton.requireEnabled();
      
      
//      // the capture view should be invisible
//      captureView.requireNotVisible();
//      
//      // the measurement frame should be visible
//      measureView.requireVisible();
      
//      main.doCapture();

//      captureView.requireVisible();
      
   }

   
   private void clickInside(JComponent target, Point p){      
      window.robot.click(target, p);//MouseButton.LEFT_BUTTON, 1);
   }
   
   @Test
   public void testClickOnFrameViewToSelectTwoPoints(){
      
      int n = main.getSelectedFrame().getMeasurements().size();

      clickInside(window.panel("MeasurementFrameView").target, new Point(100,250));
      clickInside(window.panel("MeasurementFrameView").target, new Point(150,250));

      clickInside(window.panel("MeasurementFrameView").target, new Point(370,60));
      clickInside(window.panel("MeasurementFrameView").target, new Point(200,210));
      
      assertThat(main.getSelectedFrame().getMeasurements().size(), equalTo(n+2));
      
      LengthMeasurement lastMeasurement = (LengthMeasurement) main.getSelectedFrame().getMeasurements().get(n+1);
      assertThat(lastMeasurement.startPoint.x, equalTo(370));
      assertThat(lastMeasurement.startPoint.y, equalTo(60));

      assertThat(lastMeasurement.endPoint.x, equalTo(200));
      assertThat(lastMeasurement.endPoint.y, equalTo(210));

   }

}
