package org.pm4j.sample.admin.user;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.pm4j.core.sample.admin.user.UserEditPm;

/**
 * A view for {@link UserEditPm}.
 */
public class UserEditFormHandmade extends Composite {

  public final Label nameLabel; 
  public final Text  nameText; 
  
  public final Label birthdayLabel; 
  public final Text  birthdayText; 
  
  public final Label genderLabel; 
  public final Combo  genderCombo; 
  
  public UserEditFormHandmade(Composite parent, int style) {
    super(parent, style);

    setLayout(new GridLayout(2, true));
    
    nameLabel = new Label(this, SWT.NONE);
    nameText = new Text(this, SWT.BORDER);
    
    birthdayLabel = new Label(this, SWT.NONE);
    birthdayText = new Text(this, SWT.BORDER);
    
    genderLabel = new Label(this, SWT.NONE);
    genderCombo = new Combo(this, SWT.RADIO | SWT.BORDER);
  }
  
}
