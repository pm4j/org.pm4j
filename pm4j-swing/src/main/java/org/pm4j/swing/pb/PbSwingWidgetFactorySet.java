package org.pm4j.swing.pb;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JTree;

import org.pm4j.core.pb.PbFactory;
import org.pm4j.core.pb.PbWidgetFactorySet;

/**
 * Defines a set of factories used to create and/or bind SWT widgets.
 */
public class PbSwingWidgetFactorySet implements PbWidgetFactorySet {
  public PbFactory<JButton>     pbButton = new PbJButton();
  public PbFactory<JCheckBox>   pbCheckBox = new PbJCheckBox();
  public PbFactory<JComboBox>   pbCombo = new PbJComboBox();
  public PbFactory<JLabel>      pbLabel = new PbJLabel();
  public PbFactory<JList>       pbListForOptions = new PbJListForOptions();
//  public PbFactory<JSpinner>    pbSpinner = new PbSpinner();
//  public PbFactory<StyledText> pbStyledText = new PbStyledText();
  public PbFactory<JTextField>  pbText = new PbJTextField();
//  public PbFactory<Text>       pbTextArea = new PbText(SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
  public PbFactory<JTree>       pbTree= new PbJTree();

  @Override public PbFactory<?> getPbButton()        { return pbButton; }
  @Override public PbFactory<?> getPbCheckBox()      { return pbCheckBox; }
  @Override public PbFactory<?> getPbCombo()         { return pbCombo; }
  @Override public PbFactory<?> getPbDate()          { return pbText; }
  @Override public PbFactory<?> getPbLabel()         { return pbLabel; }
  @Override public PbFactory<?> getPbListForOptions(){ return pbListForOptions; }
  @Override public PbFactory<?> getPbSpinner()       { return pbText; }
  @Override public PbFactory<?> getPbText()          { return pbText; }
  @Override public PbFactory<?> getPbTextArea()      { return pbText; }
  @Override public PbFactory<?> getPbTree()          { return pbTree; }
}