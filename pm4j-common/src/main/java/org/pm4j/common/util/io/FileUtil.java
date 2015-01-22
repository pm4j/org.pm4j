package org.pm4j.common.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.common.exception.CheckedExceptionWrapper;

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
   * Writes the given {@link String} to a file.
   *
   * @param file The file to write.
   * @param string
   */
  public static void stringToFile(File file, String string) {
    PrintWriter pw = null;
    try {
      pw = new PrintWriter(file);
      pw.print(string);
    } catch (Exception e) {
      throw new RuntimeException("Failed to write to file " + file, e);
    } finally {
      if (pw != null) pw.close();
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

  public static void copyFile(File srcFile, File targetFile) {
    if (srcFile == null || !srcFile.exists()) {
      throw new RuntimeException("Copy source file does not exist " + srcFile);
    }
    createFile(targetFile);

    BufferedInputStream reader = null;
    BufferedOutputStream writer = null;
    try {
      reader = new BufferedInputStream(new FileInputStream(srcFile));
      writer = new BufferedOutputStream(new FileOutputStream(targetFile, false));
      byte[] buff = new byte[8192];
      int numChars;
      while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
        writer.write(buff, 0, numChars);
      }
    } catch (IOException e) {
      throw new CheckedExceptionWrapper(e);
    } finally {
      try {
        if (reader != null) reader.close();
        if (writer != null) writer.close();
      } catch (IOException ex) {
        LOG.warn("Error closing files when transferring " + srcFile.getPath() + " to " + targetFile.getPath());
      }
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
        file.deleteOnExit();
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
    if (file == null) {
      return false;
    }
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
