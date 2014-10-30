package org.pm4j.common.util.io;

import java.io.File;
import java.net.URL;

/**
 * Utility methods for handling class and source files.
 *
 * @author Olaf Boede
 */
public class SrcFileUtil {

  /**
   * Provides a binaries root directory. E.g. a 'target/classes' directory.
   * <p>
   * Does only work well if the classes located in a directory structure.
   *
   * @param forClass The class used to get the responsible {@link ClassLoader}.
   * @return the binaries root directory.
   */
  public static File getClassloaderRootDir(Class<?> forClass) {
    URL rootUrl = forClass.getClassLoader().getResource(".");
    return new File(rootUrl.getFile());
  }

  public static File getDirRelativeToClassloaderRoot(File classLoaderRoot, String relPath) {
    while (relPath.startsWith("../")) {
      relPath = relPath.substring(3, relPath.length());
      classLoaderRoot = classLoaderRoot.getParentFile();
    }
    return new File(classLoaderRoot, relPath);
  }

  public static File getPkgDir(File rootDir, Class<?> forClass) {
    String relPkgName = forClass.getPackage().getName().replace('.', File.separatorChar);
    return new File(rootDir, relPkgName);
  }

}
