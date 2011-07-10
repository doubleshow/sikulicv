package org.sikuli.cv;

import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_ADAPTIVE_THRESH_MEAN_C;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_SHAPE_RECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvAdaptiveThreshold;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvReleaseStructuringElement;

import java.awt.image.BufferedImage;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;


public class ImageAnalyzer {

   IplImage input;

   ImageAnalyzer(BufferedImage input){
      //IplImage input = input;
   }
   
   

   static void computeForegroundMask(IplImage grayImage, IplImage foregroundMask){
      cvCanny(grayImage,foregroundMask,0.66*50,1.33*50,3);  

      //cvAdaptiveThreshold(foregroundMask,foregroundMask,255,CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY_INV, 5, 1);

      IplConvKernel kernel = IplConvKernel.create(3,3,1,1,CV_SHAPE_RECT,null);

      cvDilate(foregroundMask,foregroundMask,kernel,2);
      //cvErode(foregroundMask,foregroundMask,kernel,1);

      cvReleaseStructuringElement(kernel);

   }

}
