package org.pm4j.swt.pb.listener;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

/**
 * Ensures that the entered text does not exceed the given limit.
 */
public class SwtMaxTextLenVerifier implements VerifyListener {
  private final int maxLen;

  public SwtMaxTextLenVerifier(int maxLen) {
    this.maxLen = maxLen > 0
                  ? maxLen
                  : Integer.MAX_VALUE;
  }

  @Override
  public void verifyText(VerifyEvent e) {
    if (e.text != null && e.text.length() > 0 && 
        ((Text) e.widget).getText().length() >= maxLen) {
      e.doit = false;
    }
  }
}