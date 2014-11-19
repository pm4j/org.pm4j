package org.pm4j.tools.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.pm4j.common.util.io.FileUtil;
import org.pm4j.common.util.io.SrcFileAccessor;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmObject.PmMatcher;
import org.pm4j.core.xml.visibleState.VisibleStateAspectMatcher;
import org.pm4j.core.xml.visibleState.VisibleStateUtil;

/**
 * A test tool that allows to make a XML snapshot file that report the external visible
 * state of a PM tree.<br>
 * On re-execution of {@link #snapshot(PmObject, String)} it compares the documented state
 * against the state of the current PM to test.
 *
 * @author Olaf Boede
 */
public class PmSnapshotTestTool {

  private static Log LOG = LogFactory.getLog(PmSnapshotTestTool.class);

  private final Class<?> testCtxtClass;
  private SrcFileAccessor srcFileAccessor;
  private boolean overWriteMode = false;

  private Collection<PmMatcher> excludedPms = new ArrayList<PmMatcher>();
  private Collection<VisibleStateAspectMatcher> excludedAspects = new ArrayList<VisibleStateAspectMatcher>();

  /**
   * @param testCtxtClass
   *          A class that is used to determine the file location from. This is
   *          usually the test class that uses this snapshot test instance.
   */
  public PmSnapshotTestTool(Class<?> testCtxtClass) {
    assert testCtxtClass != null;
    this.testCtxtClass = testCtxtClass;
  }

  /**
   * Configures PM items to hide.
   *
   * @param hideMatchers
   * @return the tool for inline usage.
   */
  public PmSnapshotTestTool excludePms(PmMatcher... hideMatchers) {
    excludedPms.addAll(Arrays.asList(hideMatchers));
    return this;
  }

  /**
   * Configures PM aspects to hide.
   *
   * @param hideMatchers
   * @return the tool for inline usage.
   */
  public PmSnapshotTestTool excludeAspects(VisibleStateAspectMatcher... hideMatchers) {
    excludedAspects.addAll(Arrays.asList(hideMatchers));
    return this;
  }

  /**
   * Performs the snapshot logic.
   * It has the following execution modi:
   * <ol>
   *  <li>Snapshot file does not exist: The snapshot file will be generated.</li>
   *  <li>Snapshot file exists: The current PM state will be compared to the documented state.</li>
   * <ol>
  *
   * @param rootPm The root of the PM tree to verify.
   * @param fileNameBase Name of the XML file. Without '.xml' post fix. E.g. 'myTest_afterEnteringData'.
   * @return a reference to the used or generated XML file.
   */
  public File snapshot(PmObject rootPm, String fileNameBase) {
    File expectedFile = getExpectedStateFile(fileNameBase);

    if (!isOverWriteMode() && expectedFile.exists()) {
        File actualStateFile = getActualStateFile(fileNameBase);
        String actualStateXmlString = VisibleStateUtil.toXmlString(rootPm, excludedPms, excludedAspects);
        // Disturbing Windows carriage return characters need to be removed.
        String expectedStateXmlString = FileUtil.fileToString(expectedFile).replaceAll("\r\n", "\n");

        try {
          Assert.assertEquals(
              "Compare " + fileNameBase + "\nExpected: " + expectedFile + "\nCurrent: " + actualStateFile,
              expectedStateXmlString,
              actualStateXmlString);
        } catch (ComparisonFailure e) {
          try {
            LOG.info("Create actual state file " + actualStateFile);
            FileUtil.createFile(actualStateFile);
            FileUtil.stringToFile(actualStateFile, actualStateXmlString);
          } catch (RuntimeException e2) {
            // If the test gets executed for a compiled .jar it will not be possible
            // to write the actual state file.
            // The programmer will usually repeat the test in a development environment.
            // Then this error will not occur.
            LOG.error("Unable to write actual state file " + actualStateFile, e2);
          }
          throw e;
        }
    } else {
      try {
        FileUtil.createFile(expectedFile);
        VisibleStateUtil.toXmlFile(rootPm, expectedFile, excludedPms, excludedAspects);
        LOG.warn("Created snapshot file: " + expectedFile);
      } catch (Exception e) {
        throw new PmRuntimeException(rootPm, "Unable to write file " + expectedFile, e);
      }
    }

    return expectedFile;
  }

  /**
   * Provides the directory the actual PM state files will be written to.
   * <p>
   * The default implementation provides the sub directory {@link #xmlSubDirName()}
   * within the folder the '.class' file of the test class (testCtxtClass) if located in.
   * <p>
   * This location may be customized by overriding this method.
   *
   * @return the directory to write actual state files to.
   */
  protected File getActualStateDir() {
    return new File(getSrcFileAccessor().getSrcPkgDir(), xmlSubDirName());
  }

  /**
   * Provides the directory the expected PM state files will be read from.<br>
   * The initial test run will also use it to write the initial expected files.
   * <p>
   * The default implementation provides the sub directory {@link #xmlSubDirName()}
   * within the folder the '.java' file of the test class (testCtxtClass) if located in.
   * <p>
   * This location may be customized by overriding this method.
   *
   * @return the directory to handle the expected state files in.
   */
  protected File getExpectedStateDir() {
    return new File(getSrcFileAccessor().getSrcPkgDir(), xmlSubDirName());
  }

  /**
   * Provides the {@link File} the actual PM state will be written to.
   *
   * @param fileNameBase
   *          The file name base string, provided as argument of
   *          {@link #snapshot(PmObject, String)}.
   * @return the file to write.
   */
  protected File getActualStateFile(String fileNameBase) {
    return new File(getActualStateDir(), fileNameBase + ".actual.xml");
  }

  /**
   * Provides the {@link File} the expected PM state will be read from/written to.
   *
   * @param fileNameBase
   *          The file name base string, provided as argument of
   *          {@link #snapshot(PmObject, String)}.
   * @return the file to use.
   */
  protected File getExpectedStateFile(String fileNameBase) {
    return new File(getExpectedStateDir(), fileNameBase + ".xml");
  }

  /**
   * @return the name of the XML sub directory to use (in src as well as in temp dir).<br>
   *         The default implementation provides the uncapitalized test class
   *         name.
   */
  protected String xmlSubDirName() {
    return StringUtils.uncapitalize(testCtxtClass.getSimpleName());
  }

  /**
   * @return the {@link SrcFileAccessor} that provides access to source and bin path information.
   */
  protected SrcFileAccessor getSrcFileAccessor() {
    if (srcFileAccessor == null) {
      srcFileAccessor = makeSrcFileAccessor(testCtxtClass);
      assert srcFileAccessor != null;
    }
    return srcFileAccessor;
  }

  /**
   * Creates a source file structure accessor that matches your specific source/target
   * directory structure.
   *
   * @param testCtxtClass The test class context. Used to get directory information.
   * @return the {@link SrcFileAccessor} to use.
   */
  protected SrcFileAccessor makeSrcFileAccessor(Class<?> testCtxtClass) {
    return new SrcFileAccessor(testCtxtClass);
  }

  /**
   * @return the overWriteMode
   */
  public boolean isOverWriteMode() {
    return overWriteMode;
  }

  /**
   * Defines whether the tool write or overwrites the expected state files or not.
   */
  public PmSnapshotTestTool setOverWriteMode(boolean newMode) {
    this.overWriteMode = newMode;
    return this;
  }

}