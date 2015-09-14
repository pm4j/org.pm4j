package org.pm4j.core.pb;


/**
 * Defines a set of factories used to create and/or bind the standard UI widgets
 * (labels, text controls, button...).<br>
 * May be used as a place to define a consistent representation for these UI
 * widgets for an application or application area.
 * 
 * @author olaf boede
 */
public interface PbWidgetFactorySet {
  
  PbFactory<?> getPbButton();
  PbFactory<?> getPbCheckBox();
  PbFactory<?> getPbCombo();
  PbFactory<?> getPbDate();
  PbFactory<?> getPbLabel();
  PbFactory<?> getPbListForOptions();
  PbFactory<?> getPbSpinner();
  PbFactory<?> getPbText();
  PbFactory<?> getPbTextArea();
  PbFactory<?> getPbTree();

}
