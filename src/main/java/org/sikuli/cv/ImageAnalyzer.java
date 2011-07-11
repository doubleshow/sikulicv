package org.sikuli.cv;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_SIMPLEX;
import static com.googlecode.javacv.cpp.opencv_core.cvAddWeighted;
import static com.googlecode.javacv.cpp.opencv_core.cvAnd;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCountNonZero;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvLine;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvRect;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvSet;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_ADAPTIVE_THRESH_MEAN_C;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_SHAPE_RECT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvAdaptiveThreshold;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvDilate;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvErode;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMoments;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvReleaseStructuringElement;

import java.awt.image.BufferedImage;

import org.apache.log4j.Logger;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_imgproc.CvMoments;
import com.googlecode.javacv.cpp.opencv_imgproc.IplConvKernel;


public class ImageAnalyzer {

   static Logger logger = Logger.getLogger(ImageAnalyzer.class);

   
   static CvScalar green = cvScalar(0,255,0,255);
   static CvScalar blue = cvScalar(255,0,0,255);
   static CvScalar red = cvScalar(0,0,255,255);

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


   static private void computeWeightsForHalvesRelativeToPoint(IplImage foregroundMask, IplImage resultImage, 
         CvPoint origin, CvScalar color){
      
      int w = foregroundMask.width();
      int h = foregroundMask.height();
      int ix = origin.x();
      int iy = origin.y();

      CvRect upperRect = cvRect(0,0,w,iy);
      CvRect lowerRect = cvRect(0,iy,w,h-iy);
      CvRect leftRect = cvRect(0,0,ix,h);
      CvRect rightRect = cvRect(ix+1,0,w-ix,h);
      
      cvSetImageROI(foregroundMask, upperRect);      
      int upperWeight = cvCountNonZero(foregroundMask);
      
      cvSetImageROI(foregroundMask, lowerRect);
      int lowerWeight = cvCountNonZero(foregroundMask);

      cvSetImageROI(foregroundMask, leftRect);
      int leftWeight = cvCountNonZero(foregroundMask);
      
      cvSetImageROI(foregroundMask, rightRect);
      int rightWeight = cvCountNonZero(foregroundMask);

      logger.info("upper weight:" + upperWeight);
      logger.info("lower weight:" + lowerWeight);
      logger.info("left weight:" + leftWeight);
      logger.info("right weight:" + rightWeight);
      
      double verticalBalance = Math.abs(1.0*upperWeight - lowerWeight) / Math.max(upperWeight,lowerWeight);    
      logger.info("verticalBalance:" + verticalBalance);

      double horizontalBalance = Math.abs(1.0*leftWeight - rightWeight) / Math.max(leftWeight,rightWeight);    
      logger.info("horizontalBalance:" + horizontalBalance);

      
      CvFont font = new CvFont(CV_FONT_HERSHEY_SIMPLEX, 1.0, 1.0, 0, 2, CV_AA);      


      cvPutText(resultImage, ""+upperWeight, cvPoint(ix-50,iy-60), font, color);
      cvPutText(resultImage, ""+lowerWeight, cvPoint(ix-50,iy+60), font, color);
      cvPutText(resultImage, ""+leftWeight, cvPoint(ix-150,iy), font, color);
      cvPutText(resultImage, ""+rightWeight, cvPoint(ix+10,iy), font, color);   

      cvPutText(resultImage, "("+String.format("%.2f", 1-horizontalBalance)+")", cvPoint(ix+150,iy), font, color);
      cvPutText(resultImage, "("+String.format("%.2f", 1-verticalBalance)+")", cvPoint(ix-50,iy+100), font, color);
      
      cvResetImageROI(foregroundMask);
   }
   
   static IplImage computeStructure(BufferedImage inputImage){
      IplImage screenImage = IplImage.createFrom(inputImage);

      //    IplImage screenImage = IplImage.createFrom(screen);
      IplImage screenImageGray = IplImage.create(cvGetSize(screenImage), 8, 1);
      IplImage foregroundMask = IplImage.create(cvGetSize(screenImage), 8, 1);      
      cvCvtColor(screenImage, screenImageGray, CV_RGB2GRAY);

      ImageAnalyzer.computeForegroundMask(screenImageGray, foregroundMask);


      IplImage result = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvCopy(screenImage,result,foregroundMask);




      CvMoments moments = new CvMoments();
      cvMoments(foregroundMask, moments, 1);
      logger.info("m00:" + moments.m00());
      logger.info("m01:" + moments.m01());
      logger.info("m10:" + moments.m10());      
      logger.info("cy:" + moments.m01()/moments.m00());
      logger.info("cx:" + moments.m10()/moments.m00());


      // center of mass of foreground pixels
      int cy = (int) (moments.m01()/moments.m00());
      int cx = (int) (moments.m10()/moments.m00());
      CvPoint massCenter = cvPoint(cx, cy);

      int w = screenImage.width();
      int h = screenImage.height();

      // image center
      int ix = screenImage.width()/2;
      int iy = screenImage.height()/2;      
      CvPoint imageCenter = cvPoint(ix,iy);


      //cvBoundingRect()
      IplImage flippedForegroundImage = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvCopy(screenImage,flippedForegroundImage,foregroundMask);

      IplImage flippedForegroundMask = foregroundMask.clone();

      CvRect foregroundRect = cvRect(0,0,cx+cx,h);
      cvSetImageROI(flippedForegroundImage, foregroundRect);
      cvSetImageROI(flippedForegroundMask, foregroundRect);

      cvFlip(flippedForegroundImage, null, 1); // flip y-axis
      cvFlip(flippedForegroundMask, null, 1);

      cvResetImageROI(flippedForegroundImage);
      cvResetImageROI(flippedForegroundMask);

      IplImage symmetryMatchMask = IplImage.create(cvGetSize(screenImage), 8, 1);
      cvAnd(foregroundMask,flippedForegroundMask,symmetryMatchMask, null);

      IplImage symmetryMatchColorMask = IplImage.create(cvGetSize(screenImage), 8, 3);
      cvSet(symmetryMatchColorMask, red, symmetryMatchMask);


      // mixing
      cvAddWeighted(result, 0.7, flippedForegroundImage, 0.3, 0, result);
      cvAddWeighted(result, 0.5, symmetryMatchColorMask, 0.5, 0, result);

      cvCircle(result, massCenter, 20, green, 1,8,0);
      cvCircle(result, imageCenter, 20, blue, 1,8,0);

      // central vertical axis      
      cvLine(result, cvPoint(cx,0), cvPoint(cx,h), green, 2,8,0);

      // central horizontal axis      
      cvLine(result, cvPoint(0,cy), cvPoint(w,cy), green, 2,8,0);

      // central vertical axis      
      cvLine(result, cvPoint(ix,0), cvPoint(ix,h), blue, 2,8,0);

      // central horizontal axis      
      cvLine(result, cvPoint(0,iy), cvPoint(w,iy), blue, 2,8,0);

      computeWeightsForHalvesRelativeToPoint(foregroundMask, result, imageCenter, blue);
      computeWeightsForHalvesRelativeToPoint(foregroundMask, result, massCenter, green);

      return result;
   }
}
