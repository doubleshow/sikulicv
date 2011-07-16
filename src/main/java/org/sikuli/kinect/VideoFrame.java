package org.sikuli.kinect;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

interface VideoFrame {
   IplImage getIplImage();
   BufferedImage getBufferedImage();
   void write(File output) throws IOException;
}