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
   * @param fileNameBase Name of the XML file. Without 'xml' post fix. E.g. 'myTest.afterEnteringData'.
   * @return a reference to the used or generated XML file.
   */
  public File snapshot(PmObject rootPm, String fileNameBase) {
    File srcXmlDir = new File(getSrcFileAccessor().getSrcPkgDir(), xmlDirName());
    File expectedFile = new File(srcXmlDir, xmlFileName(fileNameBase));

    if (expectedFile.exists()) {
      File currentStateFile = getCurrentStateFile(fileNameBase);
      try {
        FileUtil.createFile(currentStateFile);
        writeXml(rootPm, currentStateFile);
        Assert.assertEquals(
            "Compare " + fileNameBase + "\nExpected: " + expectedFile + "\nCurrent: " + currentStateFile,
            FileUtil.fileToString(expectedFile),
            FileUtil.fileToString(currentStateFile));
        // remove currentStateFile if everything was fine:
        FileUtil.deleteFileAndEmptyParentDirs(currentStateFile);
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

  public File getCurrentStateFile(String fileNameBase) {
    File currentStateDir = new File(getSrcFileAccessor().getBinPkgDir(), xmlDirName());
    File currentStateFile = new File(currentStateDir, xmlFileName(fileNameBase));
    return currentStateFile;
  }

  public SrcFileAccessor getSrcFileAccessor() {
    return srcFileAccessor;
  }

  public String xmlDirName() {
    return StringUtils.uncapitalize(testCtxtClass.getSimpleName());
  }

  public String xmlFileName(String fileNameBase) {
    return fileNameBase + ".xml";
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
