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

/**
 * Tests for {@link PmSnapshotTestTool}.
 *
 * @author Olaf Boede
 */
public class PmSnapshotTestToolTest {

  private PmSnapshotTestTool snap = new PmSnapshotTestTool(PmSnapshotTestToolTest.class);

  /** Hint: activate that check only after test stabilization. Otherwise it may disturb.
   * But please don't forget to re-activate it before submitting your change. */
  // FIXME: does not really work reliable with maven.
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
    File file = snap.snapshot(new MiniTestPm(), "testWriteSnapshot");
    assertTrue(file.exists());
    String path = file.getPath().replace('\\', '/');
    assertTrue(path, path.endsWith("pm4j-core/src/test/java/org/pm4j/tools/test/pmSnapshotTestToolTest/testWriteSnapshot.xml"));
    try {
      assertEquals(file.getAbsolutePath(),
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
          "<conversation name=\"miniTestPm\" title=\"Test PM\">\n" +
          "    <attr name=\"stringAttr\" title=\"String Attr\"/>\n" +
          "</conversation>",
          FileUtil.fileToString(file));
    } finally {
      FileUtil.deleteFileAndEmptyParentDirs(file);
    }
  }

  @Test
  public void testWriteAndCompareSameSnapshot() {
    // create the snapshot
    File file = snap.snapshot(new MiniTestPm(), "testWriteAndCompareSameSnapshot");
    assertTrue(file.exists());

    // compare to the snapshot
    snap.snapshot(new MiniTestPm(), "testWriteAndCompareSameSnapshot");

    File currentStateFile = snap.getActualStateFile("testWriteAndCompareSameSnapshot");
    assertFalse("The current state file should not stay alive in case of no difference.\n" + currentStateFile,
                currentStateFile.exists());

    FileUtil.deleteFileAndEmptyParentDirs(file);
  }

  @Test
  public void testWriteAndCompareDifferentSnapshot() {
    MiniTestPm pm = new MiniTestPm();
    // create the snapshot
    File file = snap.snapshot(pm, "testWriteAndCompareDifferentSnapshot");
    assertTrue(file.exists());

    // modify PM and compare to the changed state
    PmAssert.setValue(pm.stringAttr, "hi");
    try {
      snap.snapshot(pm, "testWriteAndCompareDifferentSnapshot");
      Assert.fail("The snapshot compare operation should fail.");
    } catch (ComparisonFailure e) {
      File currentStateFile = snap.getActualStateFile("testWriteAndCompareDifferentSnapshot");
      assertTrue("The current state file should stay alive for manual compare operations.\n" + currentStateFile,
                 currentStateFile.exists());

      FileUtil.deleteFileAndEmptyParentDirs(currentStateFile);
    }

    FileUtil.deleteFileAndEmptyParentDirs(file);
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
