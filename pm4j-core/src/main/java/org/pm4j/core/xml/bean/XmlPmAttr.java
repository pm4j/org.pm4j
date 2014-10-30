package org.pm4j.core.xml.bean;


import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="attr")
@XmlType(name="", propOrder = { "value", "options" })
public class XmlPmAttr extends XmlPmObject {
  public String value;
  public String options;
}
