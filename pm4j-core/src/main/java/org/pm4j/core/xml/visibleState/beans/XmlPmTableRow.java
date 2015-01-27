package org.pm4j.core.xml.visibleState.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Visible PM state XML report bean.
 *
 * @author Olaf Boede
 */
@XmlRootElement(name="row")
@XmlType(propOrder = {
    // attributes (in reverese order for Java 1.6. In java 1.7 in opposite direction :-( ):
    "styleClass", "icon", "title", "enabled", "isTab", "name",
    // elements:
    "tooltip", "messages", "children" })
public class XmlPmTableRow extends XmlPmObjectBase {

  // @XmlType.propOrder seems not to support inherited properties.
  // Because of that everything is defined locally here.
  @XmlAttribute public Boolean getEnabled() { return enabled; }
  @XmlAttribute public String getIcon() { return icon; }
  @XmlAttribute public String getName() { return name; }
  @XmlAttribute public Boolean getIsTab() { return isTab; }
  @XmlAttribute public String getStyleClass() { return styleClass; }
  @XmlAttribute public String getTitle() { return title; }

  @XmlElement public String getTooltip() { return tooltip; }
  @XmlElementRef public List<XmlPmMessage> getMessages() { return messages; }
  @XmlElementRef public List<XmlPmObjectBase> getChildren() { return children; }

}
