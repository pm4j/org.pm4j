package org.pm4j.core.xml.visibleState.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

// TODO: attribute sort order is not yet under control.
// Try to run the tests with java 1.7/1.8 to verify stability.

// 'propOrder' does not work for attributes in java versions less than 1.7.
// Alphabetical is a workaround that works for all java versions.
//@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
// xmltype is used to define order of embedded xml elements. Is in java 1.6 irrelevant for attributes. See above.
@XmlType(name="", propOrder = {
    // attributes (in reverese order. Seems to work for java 1.6):
    "styleClass", "icon", "title", "enabled", "readOnly", "name",
    // elements:
    "tooltip", "messages", "children" })
@XmlSeeAlso({
    XmlPmAttr.class,
    XmlPmCommand.class,
    XmlPmConversation.class,
    XmlPmMessage.class,
    XmlPmObject.class,
    XmlPmTab.class,
    XmlPmTable.class,
    XmlPmTableCol.class,
    XmlPmTableRow.class,
    XmlPmTabSet.class
    })
public class XmlPmObjectBase {
  @XmlAttribute public String name;
  @XmlAttribute public String title;
  /** Represented as element. May contain complex content (newlines etc.). */
  public String tooltip;
  @XmlAttribute public Boolean readOnly;
  @XmlAttribute public Boolean enabled;
  @XmlAttribute public String icon;
  @XmlAttribute public String styleClass;

  @XmlElementRef
  public List<XmlPmMessage> messages = new ArrayList<XmlPmMessage>();

  @XmlElementRef
  public List<XmlPmObjectBase> children = new ArrayList<XmlPmObjectBase>();
}
