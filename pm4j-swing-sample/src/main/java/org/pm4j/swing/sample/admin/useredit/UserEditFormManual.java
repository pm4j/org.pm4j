package org.pm4j.swing.sample.admin.useredit;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserEditFormManual extends JPanel {

  private static final long serialVersionUID = 1L;

  public final JLabel       nameLabel        = new JLabel();
  public final JTextField   nameText         = new JTextField();

  public final JLabel       birthdayLabel    = new JLabel();
  public final JTextField   birthdayText     = new JTextField();

  public final JLabel       genderLabel      = new JLabel();
  public final JComboBox    genderCombo      = new JComboBox();

  public UserEditFormManual() {
    layoutComponents();
  }

  protected void layoutComponents() {
    setLayout(new GridLayout(4, 2));
    add(nameLabel);
    add(nameText);
    add(birthdayLabel);
    add(birthdayText);
    add(genderLabel);
    add(genderCombo);
  }

}
