package org.pm4j.common.util.io;

import java.io.File;

/**
 * A class that knows the file structure of a source code directory (where the Java files are located) and its related
 * binary directory (where the class files are located).
 *
 * @author Olaf Boede
 */
public class SrcFileAccessor {

  public static final String MVN_BIN_TO_MAIN_SRC_REL_PATH = "../../src/main/java";
  public static final String MVN_BIN_TO_TEST_SRC_REL_PATH = "../../src/test/java";

  private final Class<?> ctxtClass;
  private final File binRootDir;
  private final File srcRootDir;

  /**
   * A constructor using the maven bin-to-source directory settings.
   *
   * @param ctxtClass A (test) class used to identify the directory structure.
   */
  public SrcFileAccessor(Class<?> ctxtClass) {
    this(ctxtClass, MVN_BIN_TO_TEST_SRC_REL_PATH);
  }

  /**
   *
   * @param ctxtClass A (test) class used to identify the directory structure.
   * @param binToSrcPath The relative path from the binary root directory to the source code root directory.
   */
  public SrcFileAccessor(Class<?> ctxtClass, String binToSrcPath) {
    this.ctxtClass = ctxtClass;
    this.binRootDir = SrcFileUtil.getClassloaderRootDir(ctxtClass);
    this.srcRootDir = SrcFileUtil.getDirRelativeToClassloaderRoot(binRootDir, binToSrcPath);
  }

  /**
   * @return the srcDir
   */
  public File getSrcRootDir() {
    return srcRootDir;
  }

  /**
   * @param forClass The class to get the source code package directory for.
   * @return The source code directory.
   */
  public File getSrcPkgDir(Class<?> forClass) {
    return SrcFileUtil.getPkgDir(srcRootDir, forClass);
  }

  /**
   * @return The source code directory for {@link #ctxtClass}.
   */
  public File getSrcPkgDir() {
    return getSrcPkgDir(ctxtClass);
  }

  /**
   * @return the ctxtClass
   */
  public Class<?> getCtxtClass() {
    return ctxtClass;
  }

  /**
   * @return the binDir
   */
  public File getBinRootDir() {
    return binRootDir;
  }

  /**
   * @return The binary directory for {@link #ctxtClass}.
   */
  public File getBinPkgDir() {
    return SrcFileUtil.getPkgDir(binRootDir, ctxtClass);
  }

}
