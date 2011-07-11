package org.sikuli.cv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.junit.Test;
import org.junit.Test;

class FinderTestImageEditor extends JFrame implements KeyListener {


   static class FinderTestTargetView extends JScrollPane implements MouseListener {


      FinderTestTarget testCase;

      JPanel matchesPanel;

      FinderTestTargetView(FinderTestTarget testcase) throws IOException{
         setLayout(null);

         BufferedImage inputImage = testcase.getScreenImage();

         ImageIcon imageIcon = new ImageIcon(inputImage);
         JLabel label = new JLabel(imageIcon);
         label.setSize(label.getPreferredSize());
         add(label); 

         matchesPanel = new JPanel();
         matchesPanel.setOpaque(false);
         matchesPanel.setSize(label.getPreferredSize());         
         matchesPanel.setLocation(0,0);
         add(matchesPanel,0);

         setPreferredSize(label.getPreferredSize());

         addMouseListener(this);

         setTestCase(testcase);


      }


      static class MatchView extends JComponent{

         @Override
         public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            Dimension r = getSize();
            g2d.setStroke(new BasicStroke(3.0f));
            g2d.setColor(Color.green);
            g2d.drawRect(1,1,r.width-3,r.height-3);
         }
      }

      static class PointMatchView extends JComponent{

         @Override
         public void paint(Graphics g){
            Graphics2D g2d = (Graphics2D) g;
            //Dimension r = getSize();
            //g2d.setStroke(new BasicStroke(3.0f));
            g2d.setColor(Color.green);
            g2d.fillRect(0,0,20,20);
            g2d.setColor(Color.white);
            g2d.fillRect(5,5,10,10);
         }
      }




      public void setTestCase(FinderTestTarget testcase) {
         testCase = testcase;

         matchesPanel.removeAll();

         for (Point p : testcase.getGroundTruthLocations()){         
            PointMatchView tv = new PointMatchView();
            tv.setLocation(p.x-10,p.y-10);
            tv.setSize(20,20);
            matchesPanel.add(tv,0);
         }
         repaint();
      }




      @Override
      public void mouseClicked(MouseEvent arg0) {
         // TODO Auto-generated method stub

      }




      @Override
      public void mouseEntered(MouseEvent arg0) {
         // TODO Auto-generated method stub

      }




      @Override
      public void mouseExited(MouseEvent arg0) {
      }




      @Override
      public void mousePressed(MouseEvent e) {
         testCase.addGroundTruthLocation(e.getPoint());
         setTestCase(testCase); 
      }




      @Override
      public void mouseReleased(MouseEvent arg0) {
         // TODO Auto-generated method stub

      }




      public void refresh() {
         setTestCase(testCase);         
      }

   }

   class MyCellRenderer extends JLabel implements ListCellRenderer {
      public MyCellRenderer() {
         setOpaque(true);
      }

      public Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

         FinderTestTarget testCase = (FinderTestTarget) value;
         ImageIcon imageIcon;
         try {
            imageIcon = new ImageIcon(testCase.getImage());
            if (isSelected)
               this.setBorder(BorderFactory.createLineBorder(Color.red, 5));
            else
               this.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            this.setIcon(imageIcon);

         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         //          //setText(value.toString());
         //
         //          Color background;
         //          Color foreground;
         //
         //          // check if this cell represents the current DnD drop location
         //          JList.DropLocation dropLocation = list.getDropLocation();
         //          if (dropLocation != null
         //                  && !dropLocation.isInsert()
         //                  && dropLocation.getIndex() == index) {
         //
         //              background = Color.BLUE;
         //              foreground = Color.WHITE;
         //
         //          // check if this cell is selected
         //          } else if (isSelected) {
         //              background = Color.RED;
         //              foreground = Color.WHITE;
         //
         //          // unselected, and not the DnD drop location
         //          } else {
         //              background = Color.WHITE;
         //              foreground = Color.BLACK;
         //          };
         //
         //          setBackground(background);
         //          setForeground(foreground);

         return this;
      }
   }

   FinderTestTargetView testCaseView;
   JList listView;
   FinderTestImage testSuite;
   FinderTestImageEditor(FinderTestImage suite) throws IOException{

      testSuite = suite;
      FinderTestTarget testcase = suite.getTestTarget(0);
      testCaseView = new FinderTestTargetView(testcase);


      listView = new JList();
      listView.setCellRenderer(new MyCellRenderer());
      listView.setListData(suite.getTestTargets().toArray());

      listView.addListSelectionListener(new ListSelectionListener(){

         @Override
         public void valueChanged(ListSelectionEvent e) {
            int index = listView.getSelectedIndex();
            FinderTestTarget testcase = testSuite.getTestTarget(index); 
            testCaseView.setTestCase(testcase);

         }

      });
      //list.setSelectedIndex(1);
      listView.setSelectedIndex(0);

      JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listView, testCaseView);


      setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
      add(splitPane);
      pack();//setSize(800,600);

      testcase = suite.getTestTarget(0); 
      testCaseView.setTestCase(testcase);


      setFocusable(true);
      addKeyListener(this);
      listView.addKeyListener(this);
      testCaseView.addKeyListener(this);


   }

   static class TargetView extends JLabel{
      TargetView(BufferedImage target){
         ImageIcon imageIcon = new ImageIcon(target);         
         this.setIcon(imageIcon);
         setSize(target.getWidth(),target.getHeight());
      }
   }


   FinderTestTarget getCurrentTestCase(){
      int index = listView.getSelectedIndex();
      return testSuite.getTestTarget(index);
   }


   @Override
   public void keyPressed(KeyEvent e) {
      System.out.println("key pressed");
      if (e.getKeyCode() == KeyEvent.VK_S){
         System.out.println("saving");
         getCurrentTestCase().save();         
      }else if (e.getKeyCode() == KeyEvent.VK_C){
         System.out.println("clear all");         
         getCurrentTestCase().clear();
         testCaseView.refresh();
         repaint();
      }

   }

   @Override
   public void keyReleased(KeyEvent arg0) {
      // TODO Auto-generated method stub

   }

   @Override
   public void keyTyped(KeyEvent arg0) {
      // TODO Auto-generated method stub

   }




   static public class FinderTestImageEditorTestCase {

      @Test
      public void labelImage() throws IOException, InterruptedException {

         //String screenImageName = "fuzzydesktop";
         String screenImageName = "fuzzyfarmville";

         FinderTestImage image = new FinderTestImage(screenImageName);      
         FinderTestImageEditor gt = new FinderTestImageEditor(image);
         gt.setVisible(true);

         Object lock = new Object();
         synchronized (lock){
            lock.wait();
         }
      }
   }


   //   void addMatch(BufferedImage target, FindResult r){
   //      MatchView v = new MatchView();
   //      v.setBounds(r.x,r.y,r.width,r.height);
   //      viewPane.add(v,0);         
   //
   //   }   

   //   void addMatch(BufferedImage target, FindResult r){
   //      MatchView v = new MatchView();
   //      v.setBounds(r.x,r.y,r.width,r.height);
   //      viewPane.add(v,0);         
   //      
   //      TargetView tv = new TargetView(target);
   //      if (r.y > 100){
   //         tv.setLocation(r.x,r.y-r.height-2);
   //      }else{
   //         tv.setLocation(r.x,r.y+r.height+2);            
   //      }
   //      
   //      viewPane.add(tv,0);
   //      repaint();
   //   }
}



