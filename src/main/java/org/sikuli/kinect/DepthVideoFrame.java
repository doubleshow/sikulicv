package org.sikuli.kinect;

import static com.googlecode.javacv.cpp.opencv_core.cvSize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

class DepthVideoFrame extends DefaultVideoFrame{   
   DepthVideoFrame(IplImage image){
      super(image);
   }
   
   @Override
   public void write(File file) throws IOException {     
      ByteBuffer bb = getIplImage().getByteBuffer();
      FileChannel wChannel = new FileOutputStream(file).getChannel();
      wChannel.write(bb);
      wChannel.close();
   }   
   
   static public DepthVideoFrame read(File file) throws IOException{
      IplImage imgin = IplImage.create(cvSize(640,480), 16, 1);      
      ByteBuffer bbin = imgin.getByteBuffer();         
      FileChannel rChannel = new FileInputStream(file).getChannel();
      rChannel.read(bbin);
      rChannel.close();
      return new DepthVideoFrame(imgin);
   }

   
   //ByteBuffer buffer

   Calibration calibration;
   public void setCalibration(Calibration c){
      calibration = c; 
   }
   
   public float getDistanceTo(int x, int y) {
      int depthValue = getDepthValue(x,y);
      return DepthCameraModel.depthToDistanceInCentimeters(depthValue);
   }
   
   public ShortBuffer getShortBuffer() {
      IplImage img = getIplImage();
      ByteBuffer bb = img.getByteBuffer();
      return bb.order(ByteOrder.BIG_ENDIAN).asShortBuffer();
   }
   
   public int getDepthValue(int x, int y){
      int i = x + y*640;
      return getShortBuffer().get(i);
   }
}