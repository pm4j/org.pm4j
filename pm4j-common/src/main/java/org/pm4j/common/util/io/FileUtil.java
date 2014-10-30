package org.pm4j.common.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.pm4j.common.exception.CheckedExceptionWrapper;

/**
 * Convenience methods for handling {@link File}s.
 *
 * @author Olaf Boede
 */
public class FileUtil {

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
      throw new CheckedExceptionWrapper(e);
    }
  }

  /**
   * Creates a file (incl. parent path if needed).
   *
   * @param file The file to create.
   * @return the result of {@link File#createNewFile()}.
   *
   * @throws IOException
   */

  public static boolean createFile(File file) throws IOException {
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    return file.createNewFile();
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
    System.gc();
    return file.delete();
  }


  /**
   * Deletes the given file.
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
