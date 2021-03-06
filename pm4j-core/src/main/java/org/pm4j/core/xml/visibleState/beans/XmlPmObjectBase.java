package org.pm4j.core.xml.visibleState.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * Base class for visible PM state XML report beans.
 * <p>
 * Provides common properties. The XML binding definitions are located in
 * concrete sub classes.
 *
 * @author Olaf Boede
 */
@XmlSeeAlso({
    XmlPmAttr.class,
    XmlPmCommand.class,
    XmlPmConversation.class,
    XmlPmMessage.class,
    XmlPmObject.class,
    XmlPmTable.class,
    XmlPmTableCol.class,
    XmlPmTableRow.class,
    XmlPmTabSet.class
    })
public class XmlPmObjectBase {
  protected String name;
  protected String title;
  protected String shortTitle;
  /** Represented as element. May contain complex content (newlines etc.). */
  protected String tooltip;
  protected Boolean enabled;
  /** Is true if the object is used as a tab within a tab set. */
  protected Boolean isTab;
  protected String icon;
  protected String styleClass;
  protected List<XmlPmMessage> messages = new ArrayList<XmlPmMessage>();
  protected List<XmlPmObjectBase> children = new ArrayList<XmlPmObjectBase>();
  
}
