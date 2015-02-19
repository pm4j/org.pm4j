package org.pm4j.swt.pb;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.api.PmEventCallGate;
import org.pm4j.swt.pb.base.PbControlToAttrBase;
import org.pm4j.swt.pb.listener.SwtMaxTextLenVerifier;

public class PbStyledText extends PbControlToAttrBase<StyledText, PmAttrString> {

	public PbStyledText() {
		super(SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	@Override
	public StyledText makeView(Composite shell, PmAttrString pm) {
		StyledText textArea = new StyledText(shell, swtStyle);
		// FIXME olaf: that does not really work... should be done by the calling code.
		textArea.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		return textArea;
	}

  @Override
  protected PbBinding makeBinding(PmAttrString pm) {
    return new Binding();
  }

  public class Binding extends PbControlToAttrBase<StyledText, PmAttrString>.Binding
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
