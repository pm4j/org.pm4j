package org.pm4j.core.sample.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
  
  public enum ListKind { FILE, DIR, ALL };
  
  /** A helper that supports tree browsing. */
  public static List<File> getSubFiles(File dir, ListKind listDirs) {
    List<File> list = new ArrayList<File>();
    File[] fileArray = dir.listFiles(); 
    if (fileArray != null) {
      for (File f : fileArray) {
        if ( (listDirs == ListKind.ALL) ||
             (listDirs == ListKind.DIR && f.isDirectory()) ||
             (listDirs == ListKind.FILE && !f.isDirectory()) 
           ) {
          list.add(f);
        }
      }
    }
    return list;
  }

}
