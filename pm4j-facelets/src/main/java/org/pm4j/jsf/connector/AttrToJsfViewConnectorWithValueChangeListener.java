package org.pm4j.jsf.connector;

import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;

/**
 * A JSF view adapter that allows to use value change listeners for attribues.
 * <p>
 * Example usage: <pre>
 *   h:inputText
 *     value="#{pm.pmViewAdapter.valueAsString}"
 *     valueChangeListener="#{pm.pmViewAdapter.valueAsStringChangeListener}"
 * </pre>
 *
 * @author olaf boede
 */
public class AttrToJsfViewConnectorWithValueChangeListener {

  private final PmAttr<?> pmAttr;

  public AttrToJsfViewConnectorWithValueChangeListener(PmAttr<?> pmAttr) {
    this.pmAttr = pmAttr;
  }

  /**
   * A value change listener according to the JSF standard.
   *
   * @param event A value change event that contains the new value to apply.
   */
  public void valueAsStringChangeListener(ValueChangeEvent event) {
    String oldValue = StringUtils.defaultString((String)event.getOldValue(), "");
    String newValue = StringUtils.defaultString((String)event.getNewValue(), "");
    if (! oldValue.equals(newValue)) {
      pmAttr.setValueAsString(newValue);
    }
  }

  /**
   * Provides the attribute value string to render.
   *
   * @return The current value string.
   */
  public String getValueAsString() {
    return pmAttr.getValueAsString();
  }

  /**
   * A call to this setter should be ignored.
   * Unfortunately JSF calls the setter before the value change listener.
   * We have to wait for the value change listener call, because this gets done within the intended
   * JSF phase.
   *
   * @param value
   *            The value to ignore.
   */
  public void setValueAsString(String value) {
    // ignore
  }

  /**
   * A value change listener that uses the attribute value object without converting it to a string.
   * <p>
   * Useful for some JSF components that support non-string values.<br>
   * Example:<pre>
   *   h:selectBooleanCheckbox
   *     value="#{pm.pmViewAdapter.value}"
   *     valueChangeListener="#{pm.pmViewAdapter.valueChangeListener}"
   * </pre>
   *
   * @param event A value change event that contains the new value to apply.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void valueChangeListener(ValueChangeEvent event) {
    ((PmAttr)pmAttr).setValue(event.getNewValue());
  }


  /**
   * A getter that uses the attribute value object without converting it to a string.
   *
   * @return The attribute value.
   */
  public Object getValue() {
    return pmAttr.getValue();
  }

  /**
   * A call to this setter should be ignored.
   * Unfortunately JSF calls the setter before the value change listener.
   * We have to wait for the value change listener call, because this gets done within the intended
   * JSF phase.
   *
   * @param value
   *            The value to ignore.
   */
  public void setValue(Object value) {
    // ignore
  }
}
