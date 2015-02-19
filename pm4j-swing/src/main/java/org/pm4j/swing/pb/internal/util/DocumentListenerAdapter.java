package org.pm4j.swing.pb.internal.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentListenerAdapter implements DocumentListener {

	public void someUpdate(DocumentEvent e) {
	}

	public void removeUpdate(DocumentEvent e) {
		someUpdate(e);
	}

	public void insertUpdate(DocumentEvent e) {
		someUpdate(e);
	}

	public void changedUpdate(DocumentEvent e) {
		someUpdate(e);
	}
}