package org.pm4j.swing.pb;

import java.awt.Container;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventCallGate;
import org.pm4j.swing.pb.base.PbJComponentToAttrBase;
import org.pm4j.swing.pb.internal.util.MaxLenDocumentFilter;

public class PbJTextField extends PbJComponentToAttrBase<JTextField, PmAttr<?>> {

  public PbJTextField() {
  }

  @Override
  public JTextField makeView(Container parent, PmAttr<?> pm) {
    JTextField textField = new JTextField();
    parent.add(textField);
    return textField;
  }

  @Override
  protected PbBinding makeBinding(PmAttr<?> pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the modification and focus event.
   */
  public class Binding extends
      PbJComponentToAttrBase<JTextField, PmAttr<?>>.Binding implements
      DocumentListener, FocusListener {

    @Override
    public void bind() {
      super.bind();

      view.addFocusListener(this);

      Document document = view.getDocument();
      document.addDocumentListener(this);

      if (document instanceof AbstractDocument) {
        AbstractDocument abstractDocument = (AbstractDocument) document;
        DocumentFilter filter = null;
        if (pm instanceof PmAttrString) {
          int maxLen = ((PmAttrString) pm).getMaxLen();
          if (maxLen > 0) {
            filter = new MaxLenDocumentFilter(maxLen);
          }
        }
        abstractDocument.setDocumentFilter(filter);
      }
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeFocusListener(this);
      Document document = view.getDocument();
      document.removeDocumentListener(this);
      if (document instanceof AbstractDocument) {
        ((AbstractDocument)document).setDocumentFilter(null);
      }
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      view.setText(StringUtils.defaultString(pm.getValueAsString()));
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.FOCUS_LOST) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }

    private void someUpdate(DocumentEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.MODIFY) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      someUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      someUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      someUpdate(e);
    }

  }
}
