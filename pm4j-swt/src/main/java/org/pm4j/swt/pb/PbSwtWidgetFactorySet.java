package org.pm4j.swt.pb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.pm4j.core.pb.PbFactory;
import org.pm4j.core.pb.PbWidgetFactorySet;

/**
 * Defines a set of factories used to create and/or bind SWT widgets.
 */
public class PbSwtWidgetFactorySet implements PbWidgetFactorySet {
  public PbFactory<Button>     pbButton = new PbButton();
  public PbFactory<Button>     pbCheckBox = new PbCheckBox();
  public PbFactory<Combo>      pbCombo = new PbCombo();
  public PbFactory<Label>      pbLabel = new PbLabel();
  public PbFactory<List>       pbListForOptions = new PbListForOptions();
  public PbFactory<Spinner>    pbSpinner = new PbSpinner();
  public PbFactory<StyledText> pbStyledText = new PbStyledText();
  public PbFactory<Text>       pbText = new PbText();
  public PbFactory<Text>       pbTextArea = new PbText(SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
  public PbFactory<Tree>       pbTree= new PbTree();

  @Override public PbFactory<?> getPbButton()        { return pbButton; }
  @Override public PbFactory<?> getPbCheckBox()      { return pbCheckBox; }
  @Override public PbFactory<?> getPbCombo()         { return pbCombo; }
  @Override public PbFactory<?> getPbDate()          { return pbText; }
  @Override public PbFactory<?> getPbLabel()         { return pbLabel; }
  @Override public PbFactory<?> getPbListForOptions(){ return pbListForOptions; }
  @Override public PbFactory<?> getPbSpinner()       { return pbSpinner; }
  @Override public PbFactory<?> getPbText()          { return pbText; }
  @Override public PbFactory<?> getPbTextArea()      { return pbTextArea; }
  @Override public PbFactory<?> getPbTree()          { return pbTree; }
}