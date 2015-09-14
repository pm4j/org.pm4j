package org.pm4j.swing.pb;

import java.awt.Container;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.pb.PmEventCallGate;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.swing.pb.base.PbJComponentToAttrBase;

public class PbJListForOptions extends PbJComponentToAttrBase<JList, PmAttr<?>> {

  private static final String OPTION_ID_DATA_KEY = "pmOptionIds";

  private static final Logger log = LoggerFactory.getLogger(PbJListForOptions.class);

  public PbJListForOptions() {
  }

  @Override
  public JList makeView(Container parent, PmAttr<?> pm) {
    JList list = new JList();
    parent.add(list);
    return list;
  }

  @Override
  protected PbBinding makeBinding(PmAttr<?> pm) {
    return new Binding();
  }

  public class Binding extends PbJComponentToAttrBase<JList, PmAttr<?>>.Binding
      implements ListSelectionListener {

    @Override
    public void bind() {
      super.bind();
      if (!(view.getModel() instanceof DefaultListModel)) {
        view.setModel(new DefaultListModel());
      }
      view.addListSelectionListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeListSelectionListener(this);
    }

    @Override
    protected void onPmOptionSetChange(PmEvent event) {
      DefaultListModel model = getModel();
      model.removeAllElements();
      PmOptionSet os = pm.getOptionSet();
      String[] optionIdStrings = null;
      if (os != null) {
        List<PmOption> oList = os.getOptions();
        optionIdStrings = new String[oList.size()];
        for (int i = 0; i < oList.size(); ++i) {
          PmOption o = oList.get(i);
          model.addElement(o.getPmTitle());
          optionIdStrings[i] = o.getIdAsString();
        }
      }
      view.putClientProperty(OPTION_ID_DATA_KEY, optionIdStrings);
      updateSelection();
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      if (event.getSource() != view) {
        updateSelection();
      }
    }

    private void updateSelection() {
      String[] ids = StringUtils.split(pm.getValueAsString(), ",");

      if (ids != null && ids.length > 0) {
        DefaultListModel model = getModel();
        int[] indices = new int[ids.length];
        PmOptionSet optionSet = pm.getOptionSet();
        for (int i = 0; i < ids.length; ++i) {
          PmOption opt = optionSet.findOptionForId(ids[i]);
          if (opt != null) {
            indices[i] = model.indexOf(opt.getPmTitle());
          } else {
            // TODO olaf: is that really an error? what about an attribute with
            // options and free data entry?
            log.warn("No option for id '" + ids[i] + "' found in pmAttr '"
                + PmUtil.getPmLogString(pm) + "'.");
          }
        }
        view.setSelectedIndices(indices);
      }
      else {
        view.clearSelection();
      }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
      int[] selection = view.getSelectedIndices();
      String value;
      if (selection.length == 1) {
        String[] optionIds = (String[]) view.getClientProperty(OPTION_ID_DATA_KEY);
        value = optionIds[selection[0]];
      } else {
        // TODO olaf: multi selection support
        value = null;
      }
      PmEventCallGate.setValueAsString(view, pm, value);
    }

    protected DefaultListModel getModel() {
      return (DefaultListModel) view.getModel();
    }
  }
}
