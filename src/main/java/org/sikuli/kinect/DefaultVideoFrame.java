package org.sikuli.kinect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

class DefaultVideoFrame implements VideoFrame {
   
   private Calibration calibration;

   IplImage image;
   DefaultVideoFrame(IplImage image){
      this.image = image;
   }
   @Override
   public IplImage getIplImage() {
      return image;
   }
   @Override
   public BufferedImage getBufferedImage() {
      return image.getBufferedImage();
   }
   @Override
   public void write(File file) throws IOException {     
      BufferedImage bfimg = getIplImage().getBufferedImage();
      ImageIO.write(bfimg, "png", file);
   }
   public void setCalibration(Calibration calibration) {
      this.calibration = calibration;
   }
   public Calibration getCalibration() {
      return calibration;
   }      
}