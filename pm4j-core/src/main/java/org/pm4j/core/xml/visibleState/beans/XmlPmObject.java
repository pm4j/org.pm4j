package org.pm4j.core.xml.visibleState.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="pm")
// TODO: attribute sort order is not yet under control.
// Try to run the tests with java 1.7/1.8 to verify stability.

// 'propOrder' does not work for attributes in java versions less than 1.7.
// Alphabetical is a workaround that works for all java versions.
//@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
// xmltype is used to define order of embedded xml elements. Is in java 1.6 irrelevant for attributes. See above.
//@XmlType(name="", propOrder = {
    // attributes (in reverese order. Seems to work for java 1.6):
//    "styleClass", "icon", "title", "enabled", "readOnly", "name",
    // elements:
//    "tooltip", "messages", "children" })
//@XmlSeeAlso({XmlPmAttr.class, XmlPmCommand.class, XmlPmTable.class, XmlPmTabSet.class, XmlPmConversation.class})
public class XmlPmObject extends XmlPmObjectBase {
}
