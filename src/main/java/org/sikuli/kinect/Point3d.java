package org.sikuli.kinect;

import Jama.Matrix;

public class Point3d {
   double x=0;
   double y=0;
   double z=0;
   public Point3d(double xi,double yi,double zi){
      x = xi; y = yi; z = zi;
   }

   public Point3d transform(Matrix R, Matrix T){
      // convert w to RGB camera's world coordinate
      double r[][] = R.getArray();
      double t[][] = T.getArray();

      Point3d w = new Point3d(0,0,0);
      w.x = r[0][0] * x + r[0][1] * y + r[0][2] * z + r[0][3] + t[0][0];
      w.y = r[1][0] * x + r[1][1] * y + r[1][2] * z + r[1][3] + t[1][0];
      w.z = r[2][0] * x + r[2][1] * y + r[2][2] * z + r[2][3] + t[2][0];
      return w;
   }
   
   public String toString(){
      return String.format("(%.3f,%.3f,%.3f)",x,y,z);
   }
}
