package org.pm4j.core.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.xml.ToXmlVisitorCallBack;
import org.pm4j.core.xml.bean.XmlPmAttr;
import org.pm4j.core.xml.bean.XmlPmObject;

/**
 * Tests for {@link ToXmlVisitorCallBack}.
 *
 * @author Olaf Boede
 */
public class ToXmlVisitorCallBackTest {

  private ToXmlVisitorCallBack cb = new ToXmlVisitorCallBack();

  @Test
  public void testTraverseInitialPm() {
    PmVisitorApi.visit(new TestPm(), cb);

    XmlPmObject xmlRoot = cb.getXmlRoot();
    assertNotNull(xmlRoot);
    assertEquals("testPm", xmlRoot.name);
    assertEquals("Test PM", xmlRoot.title);
    // assertEquals(1, xmlRoot.children.size());
    XmlPmAttr xmlAttr = (XmlPmAttr) xmlRoot.children.get(0);
    assertEquals("boolAttr", xmlAttr.name);
    assertEquals("Boolean Attr", xmlAttr.title);
  }

  @Test
  public void testWriteXmlString() throws JAXBException, FileNotFoundException, XMLStreamException, FactoryConfigurationError {
    TestPm testPm = new TestPm();
    PmVisitorApi.visit(testPm, cb);

    StringWriter sw = new StringWriter();
    getMarshaller().marshal(cb.getXmlRoot(), sw);

    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<conversation name=\"testPm\" title=\"Test PM\">\n" +
        "    <attr name=\"boolAttr\" title=\"Boolean Attr\">\n" +
        "        <tooltip>A simple boolean attribute.</tooltip>\n" +
        "        <value>No</value>\n" +
        "        <options>|Yes|No</options>\n" +
        "    </attr>\n" +
        "    <attr name=\"requiredAttr\" title=\"Required Attr\" styleClass=\"required\"/>\n" +
        "    <attr name=\"readOnlyAttr\" readOnly=\"true\" title=\"Readonly Attr\"/>\n" +
        "    <cmd name=\"cmdDoSomething\" title=\"Do something\"/>\n" +
        "</conversation>\n"
    , sw.getBuffer().toString());
  }

  private Marshaller getMarshaller() throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance(XmlPmObject.class);
    Marshaller m = jc.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return m;
  }

  @PmTitleCfg(title = "Test PM")
  public static class TestPm extends PmConversationImpl {
    @PmTitleCfg(title = "Boolean Attr", tooltip="A simple boolean attribute.")
    @PmAttrCfg(defaultValue="false")
    public final PmAttrBoolean boolAttr = new PmAttrBooleanImpl(this);

    @PmTitleCfg(title = "Required Attr")
    @PmAttrCfg(required=true)
    public final PmAttrString requiredAttr = new PmAttrStringImpl(this);

    @PmTitleCfg(title = "Readonly Attr")
    @PmAttrCfg(readOnly=true)
    public final PmAttrString readOnlyAttr = new PmAttrStringImpl(this);

    @PmTitleCfg(title="Do something")
    public final PmCommand cmdDoSomething = new PmCommandImpl(this);

    public TestPm() {
      super(Locale.ENGLISH);
    }
  }
}
