package org.pm4j.swing.pb.internal.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

//FIXME: MaxLenDocumentFilter: silently filter or feedback ("beep") on error?
public class MaxLenDocumentFilter extends DocumentFilter {
  public final int maxLen;

  public MaxLenDocumentFilter(int maxLen) {
    this.maxLen = maxLen;
  }

  // no need for remove, as this will not increase the document len
  @Override
  public void insertString(FilterBypass fb, int offset, String string,
      AttributeSet attr) throws BadLocationException {

    // only pass through when the maxLen will not be exceeded.
    if ((fb.getDocument().getLength() + string.length()) <= maxLen) {
      super.insertString(fb, offset, string, attr);
    }
  }

  @Override
  public void replace(FilterBypass fb, int offset, int length, String text,
      AttributeSet attrs) throws BadLocationException {

    // only pass through when the maxLen will not be exceeded.
    if ((fb.getDocument().getLength() + text.length() - length) <= maxLen) {
      super.replace(fb, offset, length, text, attrs);
    }
  }

}
