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
    // attributes:
    "name", "enabled", "title", "shortTitle", "icon", "styleClass",
    // elements:
    "tooltip", "messages", "children" })
public class XmlPmTableRow extends XmlPmObjectBase {

  // @XmlType.propOrder seems not to support inherited properties.
  // Because of that everything is defined locally here.
  @XmlAttribute public Boolean getEnabled() { return enabled; }
  @XmlAttribute public String getIcon() { return icon; }
  @XmlAttribute public String getName() { return name; }
  @XmlAttribute public String getStyleClass() { return styleClass; }
  @XmlAttribute public String getTitle() { return title; }
  @XmlAttribute public String getShortTitle() { return shortTitle; }

  @XmlElement public String getTooltip() { return tooltip; }
  @XmlElementRef public List<XmlPmMessage> getMessages() { return messages; }
  @XmlElementRef public List<XmlPmObjectBase> getChildren() { return children; }

}
