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

  /**
   * @param startDir The directory to start from.
   * @param relPath A relative path that may contain '..' sub strings identifying parent directories. E.g. '../../myDir'.
   * @return The related target directory.
   */
  public static File getDirRelativeToClassloaderRoot(File startDir, String relPath) {
    while (relPath.startsWith("../")) {
      relPath = relPath.substring(3, relPath.length());
      startDir = startDir.getParentFile();
    }
    return new File(startDir, relPath);
  }

  /**
   * Provides the package directory for the given class.
   *
   * @param rootDir The root directory to start from.
   * @param forClass The class to read the package information from.
   * @return The directory that corresponds to the package.
   */
  public static File getPkgDir(File rootDir, Class<?> forClass) {
    String relPkgName = forClass.getPackage().getName().replace('.', File.separatorChar);
    return new File(rootDir, relPkgName);
  }

}
