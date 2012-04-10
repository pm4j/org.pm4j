package org.pm4j.swt.pb;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.api.PmEventCallGate;
import org.pm4j.swt.pb.base.PbControlToAttrBase;
import org.pm4j.swt.pb.listener.SwtMaxTextLenVerifier;

public class PbText extends PbControlToAttrBase<Text, PmAttr<?>> {

	public PbText() {
		this(SWT.SINGLE | SWT.BORDER);
	}

  public PbText(int style) {
    super(style);
  }

	@Override
	public Text makeView(Composite parent, PmAttr<?> pm) {
	  Text text = new Text(parent, swtStyle);
	  // TODO olaf: Thats just some demo code. Should be done in a configurable manner.
	  if ((swtStyle & SWT.MULTI) != 0 &&
	      parent.getLayout() instanceof GridLayout) {
          text.setLayoutData(new GridData(SWT.DEFAULT, 40));
	  }
	  return text;
	}

	@Override
	protected PbBinding makeBinding(PmAttr<?> pm) {
	  return new Binding();
	}

  /**
   * Handles PM events as well as the SWT modification and focus event.
   */
  public class Binding extends PbControlToAttrBase<Text, PmAttr<?>>.Binding
                       implements ModifyListener {

    private VerifyListener verifyListener;

    @Override
    public void bind() {
      super.bind();

      view.addModifyListener(this);

      if (pm instanceof PmAttrString) {
        int maxLen = ((PmAttrString)pm).getMaxLen();
        if (maxLen > 0) {
          verifyListener = new SwtMaxTextLenVerifier(maxLen);
        }
      }
    }

    @Override
    public void unbind() {
      super.unbind();
      view.removeModifyListener(this);
      if (verifyListener != null) {
        view.removeVerifyListener(verifyListener);
      }
    }

    @Override
    protected void onPmValueChange(PmEvent event) {
      view.setText(StringUtils.defaultString(pm.getValueAsString()));
    }

    @Override
    public void focusLost(FocusEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.FOCUS_LOST) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }

    @Override
    public void modifyText(ModifyEvent e) {
      if (valueUpdateEvent == ValueUpdateEvent.MODIFY) {
        PmEventCallGate.setValueAsString(view, pm, view.getText());
      }
    }
  }
}
