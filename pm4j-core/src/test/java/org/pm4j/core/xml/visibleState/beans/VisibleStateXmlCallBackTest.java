package org.pm4j.core.xml.visibleState.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.junit.Test;
import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.api.PmVisitorApi;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmTableColImpl;
import org.pm4j.core.pm.impl.PmTableImpl;
import org.pm4j.core.xml.visibleState.VisibleStateUtil;
import org.pm4j.core.xml.visibleState.beans.VisibleStateXmlCallBack;
import org.pm4j.core.xml.visibleState.beans.XmlPmAttr;
import org.pm4j.core.xml.visibleState.beans.XmlPmObjectBase;

/**
 * Tests for {@link VisibleStateXmlCallBack}.
 *
 * @author Olaf Boede
 */
public class VisibleStateXmlCallBackTest {

  private VisibleStateXmlCallBack cb = new VisibleStateXmlCallBack();

  @Test
  public void testTraverseInitialPm() {
    PmVisitorApi.visit(new TestPm(), cb);

    XmlPmObjectBase xmlRoot = cb.getXmlRoot();
    assertNotNull(xmlRoot);
    assertEquals("testPm", xmlRoot.name);
    assertEquals("Test PM", xmlRoot.title);
    // assertEquals(1, xmlRoot.children.size());
    XmlPmAttr xmlAttr = (XmlPmAttr) xmlRoot.children.get(0);
    assertEquals("boolAttr", xmlAttr.name);
    assertEquals("Boolean Attr", xmlAttr.title);
  }

  @Test
  public void testWriteTableRowPm() {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<row xmlns=\"http://org.pm4j/xml/visualState\" name=\"testRowPm\" title=\"Row\">\n" +
        "    <attr name=\"name\" title=\"Name\">\n" +
                "        <value>Hello</value>\n" +
        "    </attr>\n" +
        "</row>\n"
    , VisibleStateUtil.toXmlString(new TestPm().table.getRowPms().get(0)));
  }

  @Test
  public void testWriteTestPm() {
    assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<conversation xmlns=\"http://org.pm4j/xml/visualState\" name=\"testPm\" title=\"Test PM\">\n" +
        "    <attr name=\"boolAttr\" title=\"Boolean Attr\">\n" +
        "        <tooltip>A simple boolean attribute.</tooltip>\n" +
        "        <value>No</value>\n" +
        "        <options>|Yes|No</options>\n" +
        "    </attr>\n" +
        "    <attr name=\"requiredAttr\" title=\"Required Attr\" styleClass=\"required\"/>\n" +
        "    <attr name=\"readOnlyAttr\" readOnly=\"true\" title=\"Readonly Attr\"/>\n" +
        "    <cmd name=\"cmdDoSomething\" title=\"Do something\"/>\n" +
        "    <table name=\"table\" rows=\"2\" title=\"Table\">\n" +
        "        <column name=\"name\" title=\"Name\">\n" +
        "            <attr name=\"sortOrderAttr\" enabled=\"false\" title=\"Sort Order\">\n" +
        "                <value></value>\n" +
        // FIXME oboede: neutral and no value is redundant.
        "                <options>||Ascending|Descending</options>\n" +
        "            </attr>\n" +
        // TODO oboede: provide default icon resources and corresponding resource property entries.
        "            <cmd name=\"cmdSort\" enabled=\"false\" title=\"Sort\" icon=\"pmSortOrder.NEUTRAL_iconDisabled\"/>\n" +
        "        </column>\n" +
        "        <row name=\"testRowPm\" title=\"Row\">\n" +
        "            <attr name=\"name\" title=\"Name\">\n" +
        "                <value>Hello</value>\n" +
        "            </attr>\n" +
        "        </row>\n" +
        "        <row name=\"testRowPm\" title=\"Row\">\n" +
        "            <attr name=\"name\" title=\"Name\">\n" +
        "                <value>World</value>\n" +
        "            </attr>\n" +
        "        </row>\n" +
        "    </table>\n" +
        "</conversation>\n"
    , VisibleStateUtil.toXmlString(new TestPm()));
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

    @PmTitleCfg(title="Table")
    public final TestTablePm table = new TestTablePm(this);

    public TestPm() {
      super(Locale.ENGLISH);
    }
  }

  @PmTitleCfg(title="Row")
  @PmBeanCfg(beanClass=TestBean.class)
  public static class TestRowPm extends PmBeanBase<TestBean> {
    @PmTitleCfg(title = "Name")
    public final PmAttrString name = new PmAttrStringImpl(this);
  }

  @PmFactoryCfg(beanPmClasses=TestRowPm.class)
  static class TestTablePm extends PmTableImpl<TestRowPm, TestBean> {
    @PmTitleCfg(title = "Name")
    public final PmTableCol name = new PmTableColImpl(this);

    private List<TestBean> testBeans = new ArrayList<TestBean>(Arrays.asList(
                                new TestBean("Hello"), new TestBean("World")));

    public TestTablePm(PmObject pmParent) {
        super(pmParent);
    }

    @Override
    protected Collection<TestBean> getPmBeansImpl() {
      return testBeans;
    }
  }

  static class TestBean {
    public String name;

    public TestBean(String name) {
        this.name = name;
    }
}


}
