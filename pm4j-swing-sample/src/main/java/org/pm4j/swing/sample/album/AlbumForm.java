package org.pm4j.swing.sample.album;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


@SuppressWarnings("serial")
public class AlbumForm extends JPanel {

  public final JList albumSelectionList = new JList();
  public final JLabel titleLabel = new JLabel();
  public final JTextField  titleText = new JTextField();
  public final JLabel artistLabel= new JLabel();
  public final JTextField  artistText = new JTextField();
  public final JLabel classicalLabel= new JLabel();
  public final JCheckBox classicalCheckbox = new JCheckBox();
  public final JLabel composerLabel= new JLabel();
  public final JTextField  composerText = new JTextField();
  public final JButton btnSave = new JButton();
  public final JButton btnCancel = new JButton();

  private static JPanel newPanel(JComponent... children) {
    JPanel panel = new JPanel();
    panel.setOpaque(false);
    for (JComponent c : children) {
      panel.add(c);
    }
    return panel;
  }
  
  private void addLabelAndField(JLabel label, JComponent field) {
    add(label, new GridBagConstraints() {{anchor=EAST;}});
    add(field, new GridBagConstraints() {{gridwidth=REMAINDER;weightx=50;fill=HORIZONTAL;}});
  }

  private void layoutComponents() {
    setLayout(new GridBagLayout());
    
    // list at left (scrollable)
    JScrollPane albumListScrollPane = new JScrollPane(albumSelectionList);
    albumListScrollPane.setMinimumSize(new Dimension(10,10));
    albumListScrollPane.setPreferredSize(new Dimension(200,100));
    add(albumListScrollPane, new GridBagConstraints() {{gridheight=10;fill=BOTH;weightx=50;weighty=100;}});

    // spacer between list and details
    add(newPanel(), new GridBagConstraints() {{gridheight=10;}});
    
    // details fields at right
    addLabelAndField(titleLabel, titleText);
    addLabelAndField(artistLabel, artistText);
    addLabelAndField(classicalLabel, classicalCheckbox);
    addLabelAndField(composerLabel, composerText);
    
    // (growing) spacer between details and buttons
    add(newPanel(), new GridBagConstraints(){{gridwidth=REMAINDER;weighty=100;}});
    
    // buttons panel at right bottom
    add(newPanel(btnSave, btnCancel), new GridBagConstraints(){{gridwidth=REMAINDER;}});
  }

  public AlbumForm() {
    layoutComponents();
  }
}
