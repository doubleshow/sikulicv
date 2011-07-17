package org.sikuli.common;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

public class BundleChooser extends FileChooser {
   
   public BundleChooser(Frame parent){
      super(parent);
   }

   @Override
   public File load(){      
      File file = super.load();
      if (file == null) 
         return null;         
      
      String bundlePath = file.getAbsolutePath();            
      if( !bundlePath.endsWith(".sikuli"))
         bundlePath += ".sikuli";
      return new File(bundlePath);
   }

   
   @Override
   public File save(){      
      File file = super.save();
      if (file == null) 
         return null;         
      
      String bundlePath = file.getAbsolutePath();            
      if( !bundlePath.endsWith(".sikuli"))
         bundlePath += ".sikuli";
      return new File(bundlePath);
   }

//   
//   File file = new FileChooser(SklEditor.this).save();
//   if (file == null) 
//      return null;         
//   
//   String bundlePath = file.getAbsolutePath();            
//   if( !bundlePath.endsWith(".sikuli"))
//      bundlePath += ".sikuli";
//
//   return bundlePath;

}
