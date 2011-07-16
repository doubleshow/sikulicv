package org.sikuli.kinect;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class KinectMeasureMainFrame extends JFrame {
   
   MeasurementFileListView fileListView;
   MeasurementFrameView measureView;   
   CaptureView captureView;
   JSplitPane splitPane;
   
   class CaptureButton extends JButton implements ActionListener{
      CaptureButton(){
         super("Capture");
         setName("Capture");
         addActionListener(this);
      }

      @Override
      public void actionPerformed(ActionEvent e) {
         doCapture();
      }
   }
   CaptureButton captureButton = new CaptureButton();
   
   class LeftPanel extends JPanel{
      
      LeftPanel(){
         //setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         setLayout(new BorderLayout());
         add(captureButton, BorderLayout.NORTH);
         
         JScrollPane scrollPane = new JScrollPane(fileListView);
         scrollPane.setPreferredSize(new Dimension(180,500));
         //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         add(scrollPane, BorderLayout.CENTER);         
      }
   }
   
   class CaptureView extends VideoFrameView implements ActionListener{
            
      Timer updateTimer = new Timer(10,this);
      
      
      CaptureView(){
         super();
         setName("CaptureView");
         addMouseListener(new MouseAdapter(){
            
            @Override
            public void mouseClicked(MouseEvent e){
               if (e.getClickCount() == 2){
                  stop();
                  addNewMeasurementFrame(dualVideoFrame);
               }
            }
            
         });
      }
      
      void start(){         
         updateTimer.start();         
      }
      
      void stop(){
         updateTimer.stop();
      }

      DualVideoFrame dualVideoFrame;
      @Override
      public void actionPerformed(ActionEvent e) {
         try {
            dualVideoFrame = grabber.grab();
            setVideoFrame(dualVideoFrame.getColorFrame());
         } catch (Exception e1) {
         }         
      }
   }
   
   DualFrameGrabber grabber;
   void setDualFrameGrabber(DualFrameGrabber grabber){
      this.grabber = grabber;
   }
   
   KinectMeasureMainFrame(){
      
      captureView = new CaptureView();
      
      fileListView = new MeasurementFileListView();
      fileListView.setName("MeasurementFileListView");
      fileListView.addListSelectionListener(new ListSelectionListener(){

         @Override
         public void valueChanged(ListSelectionEvent e) {
            int index = fileListView.getSelectedIndex();
            selectFrameByIndex(index);
         }
         
      });
      
      
      measureView = new MeasurementFrameView();
      measureView.setName("MeasurementFrameView");
      
      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new LeftPanel(), measureView);
      add(splitPane);
      
      
      setResizable(false);
   }
   
   
   
   
   void doCapture(){
      splitPane.setRightComponent(captureView);      
      captureView.start();    
      captureButton.setEnabled(false);
   }
   
   void endCapture(){
      splitPane.setRightComponent(measureView);      
      captureView.stop();    
      captureButton.setEnabled(true);      
   }
   
   void selectFrameByIndex(int index){
      if (index >= 0 && index < file.getMeasurementFrames().size()){
         measureView.setMeasurementFrame(file.getMeasurementFrames().get(index));       
         fileListView.ensureIndexIsVisible(index);
      }
   }
   
   
   void loadFromBundlePath(File bundlePath) throws Exception{
      setMeasurementFile(MeasurementFile.readFromBundle(bundlePath));
      setTitle(bundlePath.getAbsolutePath());
   }
   
   MeasurementFile file;
   void setMeasurementFile(MeasurementFile file){
      this.file = file;
      fileListView.setListData(file.getMeasurementFrames().toArray());
      fileListView.setSelectedIndex(0);
      selectFrameByIndex(0);
   }
   
   MeasurementFile getMeasurementFile(){
      return file;
   }

   public MeasurementFrame getSelectedFrame() {
      return file.getMeasurementFrames().get(fileListView.getSelectedIndex());
   }

   private void addNewMeasurementFrame(DualVideoFrame dualVideoFrame) {
      MeasurementFrame newFrame = new MeasurementFrame();
      newFrame.setDualVideoFrame(dualVideoFrame);
      file.getMeasurementFrames().add(newFrame);
      
      fileListView.setListData(file.getMeasurementFrames().toArray());
      fileListView.setSelectedIndex(file.getMeasurementFrames().size()-1);
      
      endCapture();      
   }
  
}