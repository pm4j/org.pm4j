package org.pm4j.swt.pb;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.api.PmEventCallGate;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.swt.pb.base.PbControlToAttrBase;

public class PbListForOptions extends PbControlToAttrBase<List, PmAttr<?>> {

  private static final String OPTION_ID_DATA_KEY = "pmOptionIds";

  private static final Logger log = LoggerFactory.getLogger(PbListForOptions.class);

  public PbListForOptions() {
    this(SWT.NONE);
  }

  public PbListForOptions(int style) {
    super(style);
  }

  @Override
  public List makeView(Composite parent, PmAttr<?> pm) {
    return new List(parent, swtStyle);
  }

  @Override
  protected PbBinding makeBinding(PmAttr<?> pm) {
    return new Binding();
  }

  public class Binding extends PbControlToAttrBase<List, PmAttr<?>>.Binding
                       implements SelectionListener {

    @Override
    public void bind() {
      super.bind();
      view.addSelectionListener(this);
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeSelectionListener(this);
    }

    @Override
    protected void onPmOptionSetChange(PmEvent event) {
      view.removeAll();
      PmOptionSet os = pm.getOptionSet();
      String[] optionIdStrings = null;
      if (os != null) {
        java.util.List<PmOption> oList = os.getOptions();
        optionIdStrings = new String[oList.size()];
        for (int i=0; i<oList.size(); ++i) {
          PmOption o = oList.get(i);
          view.add(o.getPmTitle());
          optionIdStrings[i] = o.getIdAsString();
        }
      }
      view.setData(OPTION_ID_DATA_KEY, optionIdStrings);
      updateSelection();
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      if (event.getSource() != view) {
        updateSelection();
      }
    }

    private void updateSelection() {
      String[] ids = StringUtils.defaultString(pm.getValueAsString()).split(",");
      String[] selectionValues = new String[ids.length];

      if (ids.length > 0) {
        PmOptionSet optionSet = pm.getOptionSet();
        for (int i=0; i<ids.length; ++i) {
          PmOption opt = optionSet.findOptionForId(ids[i]);
          if (opt != null) {
            selectionValues[i] = opt.getPmTitle();
          }
          else {
            // TODO olaf: is that really an error? what about an attribute with options and free data entry?
            log.warn("No option for id '" + ids[i] + "' found in pmAttr '" + PmUtil.getPmLogString(pm) + "'.");
          }
        }
      }

      view.setSelection(selectionValues);
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
      int [] selection = view.getSelectionIndices();
      String valueString;
      if (selection.length == 1) {
        String[] optionIds = (String[])view.getData(OPTION_ID_DATA_KEY);
        valueString = optionIds[selection[0]];
      }
      else {
        // TODO olaf: multi selection support
        valueString = null;
      }
      PmEventCallGate.setValueAsString(view, pm, valueString);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      widgetSelected(e);
    }
  }
}
