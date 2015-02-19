package org.pm4j.swing.pb;

import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmOption;
import org.pm4j.swing.pb.base.PbJComponentToAttrBase;

public class PbJComboBox extends PbJComponentToAttrBase<JComboBox, PmAttr<?>> {

  public PbJComboBox() {
  }

  @Override
  public JComboBox makeView(Container parent, PmAttr<?> pm) {
    JComboBox comboBox = new JComboBox();
    parent.add(comboBox);
    return comboBox;
  }

  @Override
  protected PbBinding makeBinding(PmAttr<?> pm) {
    return new Binding();
  }

  public class Binding extends PbJComponentToAttrBase<JComboBox, PmAttr<?>>.Binding
                       implements ItemListener {

    @Override
    public void bind() {
      super.bind();
      view.addItemListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeItemListener(this);
    }
    
    /**
     * Uses the {@link PmOption} instances of the attribute directly as selection
     * items of the {@link JComboBox}.
     * <p>
     * This can be done this way, because the <code>toString</code> method of {@link PmOption}
     * provides the localized title of the option.  
     */
    @Override
    protected void onPmOptionSetChange(PmEvent event) {
      view.removeAllItems();
      for (PmOption o : pm.getOptionSet().getOptions()) {
        view.addItem(o);
      }
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      PmOption o = pm.getOptionSet().findOptionForIdString(pm.getValueAsString());
      view.setSelectedItem(o);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
      PmOption o = (PmOption)view.getSelectedItem();
      pm.setValueAsString(o != null 
    		      ? o.getIdAsString() 
    		      : null);
    }
  }

}
