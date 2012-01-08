package org.pm4j.sample.album;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class AlbumForm extends SashForm {

  public final List albumSelectionList;
  public final Label titleLabel;
  public final Text  titleText;
  public final Label artistLabel;
  public final Text  artistText;
  public final Label classicalLabel;
  public final Button classicalCheckbox;
  public final Label composerLabel;
  public final Text  composerText;
  public final Button btnSave;
  public final Button btnCancel;
  
  public AlbumForm(Composite parent, int style) {
    super(parent, style | SWT.HORIZONTAL);
    
    albumSelectionList = new List(this, SWT.BORDER);
    
    Composite detailsArea = new Composite(this, SWT.BORDER);
    detailsArea.setLayout(new GridLayout(2, false));
    
    GridData textLayoutData = new GridData(SWT.FILL, SWT.LEFT, true, false);
    
    titleLabel = new Label(detailsArea, SWT.NONE);
    titleText = new Text(detailsArea, SWT.BORDER);
    titleText.setLayoutData(textLayoutData);
    artistLabel = new Label(detailsArea, SWT.NONE);
    artistText = new Text(detailsArea, SWT.BORDER);
    artistText.setLayoutData(textLayoutData);
    classicalLabel = new Label(detailsArea, SWT.NONE);
    classicalCheckbox = new Button(detailsArea, SWT.CHECK);
    composerLabel = new Label(detailsArea, SWT.NONE);
    composerText = new Text(detailsArea, SWT.BORDER);
    composerText.setLayoutData(textLayoutData);
    
    new Label(detailsArea, SWT.NONE); // dummy for left row.
    Composite buttonArea = new Composite(detailsArea, SWT.NONE);
    buttonArea.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true));
    buttonArea.setLayout(new RowLayout(SWT.HORIZONTAL));
    btnSave = new Button(buttonArea, SWT.PUSH);
    btnCancel = new Button(buttonArea, SWT.PUSH);
  }
}
