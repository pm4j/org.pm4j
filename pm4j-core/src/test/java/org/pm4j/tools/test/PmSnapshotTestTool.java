package org.pm4j.tools.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.pm4j.common.util.io.FileUtil;
import org.pm4j.common.util.io.SrcFileAccessor;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.xml.ToXmlVisitorCallBack;
import org.pm4j.core.xml.bean.XmlPmObject;

/**
 * A test tool that allows to make an XML snapshot file that report the external visible
 * state of a PM tree.<br>
 * On re-execution of {@link #snapshot(PmObject, String)} it compares the documented state
 * against the state of the current PM to test.
 *
 * @author Olaf Boede
 */
public class PmSnapshotTestTool {

  private static Log LOG = LogFactory.getLog(PmSnapshotTestTool.class);

  private final Class<?> testCtxtClass;
  private final SrcFileAccessor srcFileAccessor;

  /**
   * @param testCtxtClass
   *          A class that is used to determine the file location from. This is
   *          usually the test class that uses this snapshot test instance.
   */
  public PmSnapshotTestTool(Class<?> testCtxtClass) {
    assert testCtxtClass != null;
    this.testCtxtClass = testCtxtClass;
    this.srcFileAccessor = new SrcFileAccessor(testCtxtClass);
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

    if (expectedFile.exists()) {
      File actualStateFile = getActualStateFile(fileNameBase);
      try {
        FileUtil.createFile(actualStateFile);
        writeXml(rootPm, actualStateFile);
        Assert.assertEquals(
            "Compare " + fileNameBase + "\nExpected: " + expectedFile + "\nCurrent: " + actualStateFile,
            FileUtil.fileToString(expectedFile),
            FileUtil.fileToString(actualStateFile));
        // remove currentStateFile if everything was fine:
        FileUtil.deleteFileAndEmptyParentDirs(actualStateFile);
      } catch (Exception e) {
        throw new PmRuntimeException("Unable to perform snapshot test", e);
      }
    } else {
      try {
        FileUtil.createFile(expectedFile);
        writeXml(rootPm, expectedFile);
        LOG.info("Created snapshot file: " + expectedFile);
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
    return new File(getSrcFileAccessor().getBinPkgDir(), xmlSubDirName());
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
    return new File(getActualStateDir(), xmlFileName(fileNameBase));
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
    return new File(getExpectedStateDir(), xmlFileName(fileNameBase));
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
   * @param fileNameBase
   *          The file name base string, provided as argument of
   *          {@link #snapshot(PmObject, String)}.
   * @return the xml file name.<br>
   *         The default implementation just adds an '.xml' post fix to the
   *         given string.
   */
  protected String xmlFileName(String fileNameBase) {
    return fileNameBase + ".xml";
  }

  /**
   * @return the {@link SrcFileAccessor} that provides access to source and bin path information.
   */
  protected SrcFileAccessor getSrcFileAccessor() {
    return srcFileAccessor;
  }

  private void writeXml(PmObject rootPm, File file) throws JAXBException, FileNotFoundException {
    ToXmlVisitorCallBack toXmlVisitorCallBack = new ToXmlVisitorCallBack();
    PmVisitorApi.visit(rootPm, toXmlVisitorCallBack,
                       PmVisitHint.SKIP_INVISIBLE, PmVisitHint.SKIP_HIDDEN_TAB_CONTENT);

    OutputStream os = new FileOutputStream(file);
    try {
      JAXBContext jc = JAXBContext.newInstance(XmlPmObject.class);
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.marshal(toXmlVisitorCallBack.getXmlRoot(), os);
    } finally {
      try { os.close(); } catch (IOException e) {}
    }
  }
}
