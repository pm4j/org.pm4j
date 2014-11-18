package org.pm4j.tools.test;

import org.pm4j.core.pm.PmObject;

/**
 * A unit test base class that supports xml snapshot tests.
 *
 * @author Olaf Boede
 */
public class PmSnapshotTestBase {

  private PmSnapshotTestTool snapshotTestTool;

  /**
   * Compares or Creates an XML snapshot for the given PM.
   *
   * @param rootPm The PM to create/compare a snapshot for.
   * @param snapshotName Name of the snapshot file (without .xml post fix).
   */
  public void snapshot(PmObject rootPm, String snapshotName) {
    snapshotTestTool.snapshot(rootPm, snapshotName);
  }

  /**
   * @return The used {@link PmSnapshotTestTool}.
   */
  protected final PmSnapshotTestTool getSnapshotTestTool() {
    if (snapshotTestTool == null) {
      snapshotTestTool = createSnapshotTestTool();
    }
    return snapshotTestTool;
  }

  /**
   * A factory method that gets called to create the used {@link PmSnapshotTestTool}.<br>
   * May be overridden to create a more specific tool.
   *
   * @return A new instance.
   */
  protected PmSnapshotTestTool createSnapshotTestTool() {
    return new PmSnapshotTestTool(getClass());
  }


}
