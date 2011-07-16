package org.sikuli.kinect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

class ColorVideoFrame extends DefaultVideoFrame{   
   ColorVideoFrame(IplImage image){
      super(image);
   }
      
   static public ColorVideoFrame read(File file) throws IOException{
      BufferedImage bfimg = ImageIO.read(file);      
      return new ColorVideoFrame(IplImage.createFrom(bfimg));
   }

}