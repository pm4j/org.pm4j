package org.pm4j.common.util.io;

import java.io.File;

public class SrcFileAccessor {

  public static final String MVN_BIN_TO_MAIN_SRC_REL_PATH = "../../src/main/java";
  public static final String MVN_BIN_TO_TEST_SRC_REL_PATH = "../../src/test/java";

  private final Class<?> ctxtClass;
  private final File binRootDir;
  private final File srcRootDir;

  public SrcFileAccessor(Class<?> ctxtClass) {
    this(ctxtClass, MVN_BIN_TO_TEST_SRC_REL_PATH);
  }

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

  public File getSrcPkgDir(Class<?> forClass) {
    return SrcFileUtil.getPkgDir(srcRootDir, forClass);
  }

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

  public File getBinPkgDir() {
    return SrcFileUtil.getPkgDir(binRootDir, ctxtClass);
  }




}
