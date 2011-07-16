package org.sikuli.kinect;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.yaml.snakeyaml.Yaml;

import Jama.Matrix;

@Root
class CameraModel {
   @Attribute
   double fx;
   @Attribute
   double fy;
   @Attribute
   double cx;
   @Attribute
   double cy;
   
   CameraModel(double fx, double fy, double cx, double cy){
      this.fx = fx; this.fy = fy; this.cx = cx; this.cy = cy;
   }
}

class ColorCameraModel extends CameraModel{
   
   ColorCameraModel(double fx, double fy, double cx, double cy) {
      super(fx, fy, cx, cy);
   }

   Point worldLocationToImageLocation(Point3d w){
      double invZ = 1.0 / w.z;
      int x = (int) (w.x * fx * invZ + cx);
      int y = (int) (w.y * fy * invZ + cy);
      return new Point(x,y);
   }
}

@Root
class DepthCameraModel extends CameraModel{

   static float k1 = 0.1236f;
   static float k2 = 2842.5f;
   static float k3 = 1.1863f;
   static float k4 = 0.0370f;
   
   static {
      calculateLookup();
   }
   
   static public float depthToDistanceInCentimeters(int depthValue){
      return distancePixelsLookup[depthValue];
   }
   
   static private float rawToCentimeters(int raw) {
      return (float) (100 * (k1 * Math.tan((1f*raw / k2) + k3) - k4));
   }

   static private float[] distancePixelsLookup;

   static private void calculateLookup(){
      distancePixelsLookup = new float[2048];
      for(int i = 0; i < 2048; i++){
         if (i > 1000) {
            distancePixelsLookup[i] = 0;
         } else {
            distancePixelsLookup[i] = rawToCentimeters(i);
         }
      }
   }

   
   DepthCameraModel(double fx, double fy, double cx, double cy) {
      super(fx, fy, cx, cy);
   }
   
   Point3d imageLocationToWorldLocation(int x, int y, int depthValue){
      double wz = depthToDistanceInCentimeters(depthValue)*0.01;   // centimeter -> meter
      double wx = (x - cx) * wz / fx;
      double wy = (y - cy) * wz / fy;      
      return new Point3d(wx,wy,wz);
   }
}


class CameraModelTransform {   
   Matrix R;
   Matrix T;
}

@Root
public class Calibration {
      
   @Element
   DepthCameraModel depthCameraModel;
   
   @Element
   ColorCameraModel colorCameraModel;
   CameraModelTransform transform;
   
   private static String readFileAsString(File file) throws java.io.IOException{
      byte[] buffer = new byte[(int) file.length()];
      BufferedInputStream f = null;
      try {
          f = new BufferedInputStream(new FileInputStream(file));
          f.read(buffer);
      } finally {
          if (f != null) try { f.close(); } catch (IOException ignored) { }
      }
      return new String(buffer);
  }
   
   static Calibration loadFromFile(File file) throws IOException{
      Calibration calibration = new Calibration();
      
      String content = readFileAsString(file);
      String input = content.replaceAll("opencv-matrix","org.sikuli.kinect.opencvmatrix");
      input = input.replaceAll("%YAML:1.0","");
      Yaml yaml = new Yaml();
      Object data = yaml.load(input);
      Map map = (HashMap) data;
      
      double fx,fy,cx,cy;      
      opencvmatrix m = (opencvmatrix) map.get("rgb_intrinsics");      
      fx = m.getData().get(0);
      fy = m.getData().get(2);
      cx = m.getData().get(4);
      cy = m.getData().get(5);      
      calibration.colorCameraModel = new ColorCameraModel(fx,fy,cx,cy);      
      
      m = (opencvmatrix) map.get("depth_intrinsics");      
      fx = m.getData().get(0);
      fy = m.getData().get(2);
      cx = m.getData().get(4);
      cy = m.getData().get(5);      
      calibration.depthCameraModel = new DepthCameraModel(fx,fy,cx,cy);
      
      m = (opencvmatrix) map.get("R");
      Matrix R = new Matrix(4,4);
      R.set(0,0,m.getData().get(0));
      R.set(0,1,m.getData().get(1));
      R.set(0,2,m.getData().get(2));
      R.set(1,0,m.getData().get(3));
      R.set(1,1,m.getData().get(4));
      R.set(1,2,m.getData().get(5));
      R.set(2,0,m.getData().get(6));
      R.set(2,1,m.getData().get(7));
      R.set(2,2,m.getData().get(8));
      R.set(3,3,1);
      
      m = (opencvmatrix) map.get("T");
      Matrix T = new Matrix(4,1);
      T.set(0,0,m.getData().get(0));
      T.set(1,0,m.getData().get(1));
      T.set(2,0,m.getData().get(2));
      T.set(3,0,1);
      
      CameraModelTransform transform = new CameraModelTransform();
//      transform.R = R;
//      transform.T = T;
            
      double rawI[][] = {{-1,0,0,0},{0,1,0,0},{0,0,-1,0},{0,0,0,1}};
      Matrix I = new Matrix(rawI);
      Matrix Iinv = I.inverse();
      Matrix Rinv = R.inverse();
      transform.R = Iinv.times(Rinv).times(I);         
      transform.T = I.times(T);
      
      calibration.transform = transform;      
      return calibration;
   }
   
}
