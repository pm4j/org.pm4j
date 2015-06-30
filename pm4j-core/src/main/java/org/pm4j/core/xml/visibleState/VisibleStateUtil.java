package org.pm4j.core.xml.visibleState;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.pm4j.common.util.io.FileUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmObject.PmMatcher;
import org.pm4j.core.pm.api.PmVisitorApi.PmVisitHint;
import org.pm4j.core.pm.impl.PmVisitorImpl;
import org.pm4j.core.xml.visibleState.beans.VisibleStateXmlCallBack;
import org.pm4j.core.xml.visibleState.beans.XmlPmObject;
import org.pm4j.core.xml.visibleState.beans.XmlPmObjectBase;

/**
 * Visible state xml helper methods.
 *
 * @author Olaf Boede
 */
public class VisibleStateUtil {

  /**
   * @param rootPm The PM to report.
   * @param excludedPms Rules for the PMs to hide.
   * @param excludedAspects Rules for the aspects to hide.
   * @return The corresponding XML beans.
   */
  public static XmlPmObjectBase toXmlObject(
        PmObject rootPm,
        Collection<PmMatcher> excludedPms,
        Collection<VisibleStateAspectMatcher> excludedAspects) {
    VisibleStateXmlCallBack xmlCallBack = new VisibleStateXmlCallBack()
                            .exclude(excludedAspects);
    PmVisitorImpl visitor = new PmVisitorImpl(xmlCallBack)
                            .hints(PmVisitHint.SKIP_CONVERSATION,
                                   PmVisitHint.SKIP_INVISIBLE)
                            .exclude(excludedPms);
    visitor.visit(rootPm);

    return xmlCallBack.getXmlRoot();
  }

  /**
   * @param rootPm The PM to report.
   * @param file The file to write to.
   * @param excludedPms Rules for the PMs to hide.
   * @param excludedAspects Rules for the aspects to hide.
   */
  public static void toXmlFile(
        PmObject rootPm,
        File file,
        Collection<PmMatcher> excludedPms,
        Collection<VisibleStateAspectMatcher> excludedAspects) {
    XmlPmObjectBase xmlObject = toXmlObject(rootPm, excludedPms, excludedAspects);
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      JAXBContext jc = JAXBContext.newInstance(XmlPmObject.class);
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.setProperty(Marshaller.JAXB_ENCODING, FileUtil.UTF_8);
      m.marshal(xmlObject, os);
    } catch (Exception e) {
      throw new PmRuntimeException(rootPm, "Failed to generate XML file " + file, e);
    } finally {
      try { if (os != null) os.close(); } catch (IOException e) {}
    }
  }

  /**
   * @param rootPm The PM to report.
   * @param excludedPms Rules for the PMs to hide.
   * @param excludedAspects Rules for the aspects to hide.
   * @return the XML string.
   */
  public static String toXmlString(
        PmObject rootPm,
        Collection<PmMatcher> excludedPms,
        Collection<VisibleStateAspectMatcher> excludedAspects) {
    XmlPmObjectBase xmlObject = toXmlObject(rootPm, excludedPms, excludedAspects);

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      JAXBContext jc = JAXBContext.newInstance(XmlPmObject.class);
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.marshal(xmlObject, os);
      return os.toString(FileUtil.UTF_8).trim();
    } catch (Exception e) {
      throw new PmRuntimeException(rootPm, "Failed to generate XML.", e);
    }
  }

    /**
     * @param rootPm The PM to report.
     * @return the XML string.
     */
    @SuppressWarnings("unchecked")
    public static String toXmlString(PmObject rootPm) {
        return toXmlString(rootPm, Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

}
