package org.pm4j.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Locale;

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.pm4j.common.util.io.FileUtil;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.xml.visibleState.VisibleStateAspect;

/**
 * Tests for {@link PmSnapshotTestTool}.
 *
 * @author Olaf Boede
 */
public class PmSnapshotTestToolTest {

  private PmSnapshotTestTool snap = new PmSnapshotTestTool(PmSnapshotTestToolTest.class);
  private MiniTestPm pm = new MiniTestPm();


  /** Hint: activate that check only after test stabilization. Otherwise it may disturb.
   * But please don't forget to re-activate it before submitting your change. */
  // TODO: does not really work reliable with maven.
  // @After
  public void noTempFilesShouldRemainInSrcDir() {
    File srcXmlDir = new File(snap.getSrcFileAccessor().getSrcPkgDir(), snap.xmlSubDirName());
    File currentStateXmlDir = new File(snap.getSrcFileAccessor().getBinPkgDir(), snap.xmlSubDirName());

    assertFalse("No temporary expectation files shold exist after test execution: " + srcXmlDir,
                srcXmlDir.exists());
    assertFalse("No temporary current state files shold exist after test execution: " + currentStateXmlDir,
                currentStateXmlDir.exists());
  }

  @Test
  public void testWriteSnapshot() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    File file = null;
    try {
      file = snap.snapshot(pm, "testWriteSnapshot");
      assertEquals(file.getAbsolutePath(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
          "<conversation xmlns=\"http://org.pm4j/xml/visibleState\" name=\"miniTestPm\" title=\"Test PM\">\n" +
          "    <attr name=\"stringAttr\" title=\"String Attr\"/>\n" +
          "</conversation>",
          FileUtil.fileToString(file));
    } finally {
      if (file != null) {
        FileUtil.deleteFileAndEmptyParentDirs(file);
      }
    }
  }

  @Test
  public void testExcludedTitleByClassAndFieldName() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    File file = null;
    try {
      snap.exclude(MiniTestPm.class, "stringAttr", VisibleStateAspect.TITLE);
      file = snap.snapshot(pm, "testWriteSnapshot");
      assertEquals(file.getAbsolutePath(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
          "<conversation xmlns=\"http://org.pm4j/xml/visibleState\" name=\"miniTestPm\" title=\"Test PM\">\n" +
          "    <attr name=\"stringAttr\"/>\n" +
          "</conversation>",
          FileUtil.fileToString(file));
    } finally {
      if (file != null) {
        FileUtil.deleteFileAndEmptyParentDirs(file);
      }
    }
  }

  @Test
  public void testExcludedTitleByPmRef() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    File file = null;
    try {
      pm.stringAttr.setPmTitle("String Attr");
      snap.exclude(pm.stringAttr, VisibleStateAspect.TITLE);
      file = snap.snapshot(pm, "testWriteSnapshot");
      assertEquals(file.getAbsolutePath(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
          "<conversation xmlns=\"http://org.pm4j/xml/visibleState\" name=\"miniTestPm\" title=\"Test PM\">\n" +
          "    <attr name=\"stringAttr\"/>\n" +
          "</conversation>",
          FileUtil.fileToString(file));
    } finally {
      if (file != null) {
        FileUtil.deleteFileAndEmptyParentDirs(file);
      }
    }
  }

  @Test
  public void testWriteUtf8Snapshot() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.WRITE);
    File file = null;

    try {
      pm.stringAttr.setPmTitle("|HAMBURG SÜD|ALIANÇA");
      file = snap.snapshot(pm, "testWriteSnapshotUtf8");
      assertEquals(file.getAbsolutePath(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
          "<conversation xmlns=\"http://org.pm4j/xml/visibleState\" name=\"miniTestPm\" title=\"Test PM\">\n" +
          "    <attr name=\"stringAttr\" title=\"|HAMBURG SÜD|ALIANÇA\"/>\n" +
          "</conversation>",
          FileUtil.fileToString(file.getAbsoluteFile()));
          pm = null;
    } finally {
      if (file != null) {
        FileUtil.deleteFileAndEmptyParentDirs(file);
      }
    }
  }

  @Test
  public void testWriteAndCompareSameSnapshot() {
    // create the snapshot
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    File file = null;

    try {
      file = snap.snapshot(pm, "testWriteAndCompareSameSnapshot");
      assertTrue(file.exists());

      // compare to the snapshot
      snap.snapshot(pm, "testWriteAndCompareSameSnapshot");

      File currentStateFile = snap.getActualStateFile("testWriteAndCompareSameSnapshot");
      assertFalse("A current state file should not exist in case of no difference.\n" + currentStateFile,
              currentStateFile.exists());
    } finally {
      FileUtil.deleteFileAndEmptyParentDirs(file);
    }
  }

  @Test(expected= AssertionError.class)
  public void testWriteSnapshotInStrictMode() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.STRICT);
    snap.snapshot(pm, "testWriteSnapshotInStrictMode");
  }

  @Test
  public void testWriteExistingSnapshot() {

    // create the snapshot
    final String fileBaseName = "overrideMe";
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    File expectedStateFileSrc = null;
    File expectedStateFile = getExpectedFile(fileBaseName);

    try {
      expectedStateFileSrc = snap.snapshot(pm, fileBaseName);
      assertTrue(expectedStateFileSrc.exists());

      // modify PM and compare to the changed state
      PmAssert.setValue(pm.stringAttr, "hi");

      // Copy the generated expected state file to the binary directory (usually done by the build process).
      // After that we can simulate a regular compare operation.
      FileUtil.copyFile(expectedStateFileSrc, expectedStateFile);

      // override snapshot
      snap.setTestMode(PmSnapshotTestTool.TestMode.WRITE);
      snap.snapshot(pm, fileBaseName);

      // Copy the overridden expected state
      FileUtil.copyFile(expectedStateFileSrc, expectedStateFile);

      // now test in STRICT MODE
      snap.setTestMode(PmSnapshotTestTool.TestMode.STRICT);
      snap.snapshot(pm, "overrideMe");

    } finally {
      FileUtil.deleteFileAndEmptyParentDirs(expectedStateFile);
      FileUtil.deleteFileAndEmptyParentDirs(expectedStateFileSrc);
    }
  }

  @Test
  public void testWriteAndCompareDifferentSnapshot() {
    snap.setTestMode(PmSnapshotTestTool.TestMode.AUTO_CREATE);
    final String fileBaseName = "testWriteAndCompareDifferentSnapshot";
    File expectedStateFileSrc = snap.getExpectedStateSrcFile(fileBaseName);
    File currentStateFile = snap.getActualStateFile(fileBaseName);
    File expectedStateFile = getExpectedFile(fileBaseName );

    try {
      assertFalse("File should not exist: " + expectedStateFile, expectedStateFile.exists());
      assertFalse("File should not exist: " + currentStateFile,  currentStateFile.exists());

      // create the snapshot
      File createdSrcFile = snap.snapshot(pm, fileBaseName);
      assertTrue("File should exist: " + createdSrcFile, createdSrcFile.exists());
      assertEquals(expectedStateFileSrc.getPath(), createdSrcFile.getPath());


      // Copy the generated expected state file to the binary directory (usually done by the build process).
      // After that we can simulate a regular compare operation.
      FileUtil.copyFile(expectedStateFileSrc, expectedStateFile);

      // modify PM and compare to the changed state
      PmAssert.setValue(pm.stringAttr, "hi");
      try {
        snap.snapshot(pm, fileBaseName);
        Assert.fail("The snapshot compare operation should fail.");
      } catch (ComparisonFailure e) {
        assertTrue("The current state file should stay alive for manual compare operations.\n" + currentStateFile,
                   currentStateFile.exists());
      }
    } finally {
      FileUtil.deleteFileAndEmptyParentDirs(expectedStateFileSrc);
      FileUtil.deleteFileAndEmptyParentDirs(expectedStateFile);
      FileUtil.deleteFileAndEmptyParentDirs(currentStateFile);
    }

  }

  /**
   * This method is to determine possible target expected file destination as {@link PmSnapshotTestTool#getExpectedStateDir()} returns {@code null} when path was not created.
   *
   * @param fileBaseName file base name
   * @return expected file descriptor
   */
  private File getExpectedFile(String fileBaseName) {
    return new File(new File(snap.getSrcFileAccessor().getBinPkgDir(), snap.xmlSubDirName()), fileBaseName + ".xml");
  }


  @PmTitleCfg(title = "Test PM")
  public static class MiniTestPm extends PmConversationImpl {
    @PmTitleCfg(title = "String Attr")
    public final PmAttrString stringAttr = new PmAttrStringImpl(this);

    public MiniTestPm() {
      super(Locale.ENGLISH);
    }
  }
}
