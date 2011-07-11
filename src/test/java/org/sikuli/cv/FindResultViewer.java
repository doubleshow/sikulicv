package org.sikuli.cv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


class FindResultViewer extends InputImageViewer{
   

   
   FindResultViewer(BufferedImage inputImage){
      super(inputImage);
   }
   
   static class TargetView extends JLabel{
      TargetView(BufferedImage target){
         ImageIcon imageIcon = new ImageIcon(target);
         this.setIcon(imageIcon);
         setSize(target.getWidth(),target.getHeight());
      }
   }
   
   static class MatchView extends JComponent{
      
      @Override
      public void paint(Graphics g){
         Graphics2D g2d = (Graphics2D) g;
         Dimension r = getSize();
         g2d.setColor(Color.black);
         g2d.drawRect(0,0,r.width-1,r.height-1);
         g2d.setColor(Color.white);
         g2d.drawRect(1,1,r.width-3,r.height-3);
         g2d.setColor(Color.black);
         g2d.drawRect(2,2,r.width-5,r.height-5);

      }
   }
   
   void addMatch(BufferedImage target, FindResult r){
      MatchView v = new MatchView();
      v.setBounds(r.x,r.y,r.width,r.height);
      viewPane.add(v,0);
      
      JLabel score = new JLabel();
      score.setText(String.format("%.3f",r.score));
      score.setSize(score.getPreferredSize());
      score.setLocation(r.x+r.width,r.y);
      score.setBackground(new Color(1.0f,1.0f,0.0f,0.5f));
      score.setOpaque(true);
      viewPane.add(score,0);
      
//      TargetView tv = new TargetView(target);
//      if (r.y > 100){
//         tv.setLocation(r.x,r.y-r.height-2);
//      }else{
//         tv.setLocation(r.x,r.y+r.height+2);            
//      }
//      
//      viewPane.add(tv,0);
      repaint();
   }
}