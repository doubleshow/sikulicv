package org.sikuli.kinect;

public class KinectMath {
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

}
