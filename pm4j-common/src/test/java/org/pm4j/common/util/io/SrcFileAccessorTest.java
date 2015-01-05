package org.pm4j.common.util.io;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.junit.Ignore;

public class SrcFileAccessorTest {

  @Test
  @Ignore
  public void testGetSrcPkgDir() {
    SrcFileAccessor sfa = new SrcFileAccessor(SrcFileAccessorTest.class);
    File pkgDir = sfa.getSrcPkgDir();
    assertTrue(new File(pkgDir, getClass().getSimpleName()+".java").exists());
  }
}
