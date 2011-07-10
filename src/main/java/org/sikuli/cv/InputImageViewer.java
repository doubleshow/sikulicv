package org.sikuli.cv;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class InputImageViewer extends JFrame {

   protected ViewPane viewPane;
   static class ViewPane extends JPanel{      
      ViewPane(BufferedImage inputImage){
         setLayout(null);
         
         ImageIcon imageIcon = new ImageIcon(inputImage);
         JLabel label = new JLabel(imageIcon);
         label.setSize(label.getPreferredSize());
         add(label); 
         
         setPreferredSize(label.getPreferredSize());
      }
   }

   public InputImageViewer(BufferedImage inputImage) {
      super();
      
      viewPane = new ViewPane(inputImage);
      
      JScrollPane scrollPane = new JScrollPane(viewPane);
      scrollPane.setPreferredSize(new Dimension(800,600));
      
      setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
      add(scrollPane);
      setSize(inputImage.getWidth()+30,inputImage.getHeight()+30);
   }

}