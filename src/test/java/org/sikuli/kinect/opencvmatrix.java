package org.sikuli.kinect;

import java.util.List;

public class opencvmatrix{
   private int rows;
   private int cols;
   private String dt;
   private List<Double> data;
   public void setRows(int rows) {
      this.rows = rows;
   }
   public int getRows() {
      return rows;
   }
   public void setCols(int cols) {
      this.cols = cols;
   }
   public int getCols() {
      return cols;
   }
   public void setDt(String dt) {
      this.dt = dt;
   }
   public String getDt() {
      return dt;
   }
   public void setData(List<Double> data) {
      this.data = data;
   }
   public List<Double> getData() {
      return data;
   }
}