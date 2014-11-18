package org.pm4j.common.util.io;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class SrcFileUtilTest {

  @Test
  public void testLocateClassFile() {
    File rootDir = SrcFileUtil.getClassloaderRootDir(SrcFileUtilTest.class);
    File pkgDir = SrcFileUtil.getPkgDir(rootDir, SrcFileUtilTest.class);
    assertTrue(pkgDir.isDirectory());
    File classFile = new File(pkgDir, SrcFileUtilTest.class.getSimpleName() + ".class");
    assertTrue(classFile.exists());
  }

  @Test
  public void testLocateSrcFile() {
    File binDir = SrcFileUtil.getClassloaderRootDir(SrcFileUtilTest.class);
    File srcRootDir = SrcFileUtil.getDirRelativeToClassloaderRoot(binDir, "../../src/test/java");
    assertTrue(srcRootDir.isDirectory());
    File pkgDir = SrcFileUtil.getPkgDir(srcRootDir, SrcFileUtilTest.class);
    assertTrue(pkgDir.isDirectory());
    File classFile = new File(pkgDir, SrcFileUtilTest.class.getSimpleName() + ".java");
    assertTrue(classFile.getAbsolutePath(), classFile.exists());
  }
}
