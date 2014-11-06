package org.pm4j.core.xml.visibleState.beans;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name="message")
public class XmlPmMessage {

  @XmlAttribute public String severity;
  @XmlValue public String title;

}
