package org.sikuli.cv;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Test;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;

public class JavaCVSandBox {
   
    static Logger logger = Logger.getLogger(JavaCVSandBox.class);

    public static void smooth(String filename) { 
        IplImage image = cvLoadImage(filename);
        if (image != null) {
            cvSmooth(image, image, CV_GAUSSIAN, 3);
            cvSaveImage(filename, image);
            cvReleaseImage(image);
        }
    }
    
    @Test
    public void testResizeImage() throws InterruptedException {
       IplImage screen = cvLoadImage("screen.png");

       CvSize size = cvSize(screen.width()/2,screen.height()/2);
       IplImage smaller = IplImage.create(size, 8, 3);
       cvResize(screen, smaller, CV_INTER_LINEAR);
       
       CanvasFrame frame = new CanvasFrame("Some Title");
       frame.showImage(smaller);
       
       Object lock = new Object();
       synchronized(lock){
          lock.wait();
       }
       
       cvReleaseImage(screen);
       cvReleaseImage(smaller);
    }
    
    @Test
    public void testTemplateMatch() throws InterruptedException{
       
       // Set up a simple configuration that logs on the console.
       BasicConfigurator.configure();

       
       IplImage screen = cvLoadImage("screen.png");
       IplImage template = cvLoadImage("template.png");
       
       // Allocate Output Images:
       int iwidth = screen.width() - template.width() + 1;
       int iheight = screen.height() - template.height() + 1;
       
       IplImage result = IplImage.create(cvSize(iwidth,iheight), 32, 1);
       
       opencv_imgproc.cvMatchTemplate(screen, template, result, CV_TM_CCORR_NORMED);
       
       double min[] = new double[1];
       double max[] = new double[1];
       CvPoint minPoint = new CvPoint(2);
       CvPoint maxPoint = new CvPoint(2);
       
       opencv_core.cvMinMaxLoc(result, min, max, minPoint, maxPoint, null);       
  
       int x = maxPoint.x();
       int y = maxPoint.y();
       int w = template.width();
       int h = template.height();
       cvRectangle(screen, cvPoint(x,y), cvPoint(x+w, y+h), CvScalar.RED, 1, CV_AA, 0);
       
       logger.info(maxPoint);
       
       
       CanvasFrame frame = new CanvasFrame("Some Title");
       frame.showImage(screen);
       
       Object lock = new Object();
       synchronized(lock){
          lock.wait();
       }
       
       cvReleaseImage(screen);
       cvReleaseImage(template);
       
    }
    
    @Test
    public void testSmooth(){
       
       JavaCVSandBox.smooth("screen.png");
       
    }
}