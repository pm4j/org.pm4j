package org.pm4j.swing.util;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JFrameUtil {

  public static JFrame showInMainFrame(Component component, String frameTitle) {
	  JFrame jfFrame = showInFrame(component, frameTitle);
	  jfFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  return jfFrame;
  }

  public static JFrame showInFrame(final Component component, final String frameTitle) {
		final JFrame jFrame = new JFrame(frameTitle);
		jFrame.getContentPane().add(component);
		Runnable runner = new Runnable() {
			public void run() {

				jFrame.pack();
				jFrame.setVisible(true);
			}
		};
		SwingUtilities.invokeLater(runner);
		return jFrame;
	}
}
