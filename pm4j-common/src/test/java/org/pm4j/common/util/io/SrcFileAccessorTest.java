package org.pm4j.common.util.io;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class SrcFileAccessorTest {

  @Test
  public void testGetSrcPkgDir() {
    SrcFileAccessor sfa = new SrcFileAccessor(SrcFileAccessorTest.class);
    File pkgDir = sfa.getSrcPkgDir();
    assertTrue(new File(pkgDir, getClass().getSimpleName()+".java").exists());
  }
}
