package org.pm4j.common.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenience methods for handling {@link File}s.
 *
 * @author Olaf Boede
 */
public class FileUtil {

  private static final Log LOG = LogFactory.getLog(FileUtil.class);

  /**
   * Reads a complete text file.
   *
   * @param file The text file to read.
   * @return the file content.
   */
  public static String fileToString(File file) {
    try {
      return new Scanner(file).useDelimiter("\\Z").next();
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Failed to read file: " + file, e);
    }
  }

  /**
   * Creates a file (incl. parent path if needed).
   * <p>
   * It's ok if the file already exists.
   * TODO: check if the file will be cleared in that case.
   *
   * @param file The file to create.
   * @return the result of {@link File#createNewFile()}.
   *
   * @throws IOException
   */

  public static void createFile(File file) {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    try {
      file.createNewFile();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create file: " + file, e);
    }
  }

  /**
   * Deletes the given file.
   * <p>
   * Triggers internally a GC loop. That seems to be required on Windows systems.
   *
   * @param file The file to delete.
   * @return the result of {@link File#delete()}.
   */
  public static boolean delete(File file) {
    if (file.exists()) {
      // TODO oboede: workaround for Windows. Find a reliable way to delete a file.
      System.gc();
      if (! file.delete()) {
        LOG.warn("Can't delete file: " + file);
        return false;
      }
    }
    return true;
  }


  /**
   * Deletes the given file and all empty parent directories.
   * <p>
   * Triggers internally a GC loop. That seems to be required on Windows systems.
   *
   * @param file The file to delete.
   * @return the result of {@link File#delete()}.
   */
  public static boolean deleteFileAndEmptyParentDirs(File file) {
    boolean deleted = delete(file);
    deleteEmptyDirAndEmptyParentDirs(file.getParentFile());
    return deleted;
  }

  /**
   * Deletes the given directory and all its parents until a non empty directory was found.
   *
   * @param dir The directory to cleanup.
   */
  public static void deleteEmptyDirAndEmptyParentDirs(File dir) {
    if (dir == null) {
      return;
    }

    if (dir.exists() && dir.listFiles().length == 0) {
      if (dir.delete()) {
        deleteEmptyDirAndEmptyParentDirs(dir.getParentFile());
      }
    }
  }
}
