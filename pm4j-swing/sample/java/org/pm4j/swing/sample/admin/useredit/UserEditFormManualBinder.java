package org.pm4j.swing.sample.admin.useredit;

import java.awt.Container;
import java.awt.Frame;

import javax.swing.JDialog;

import org.pm4j.core.sample.admin.user.UserEditPm;
import org.pm4j.core.sample.admin.user.service.UserTestDataFactory;
import org.pm4j.swing.pb.base.PbFormBase;

/**
 * Binds the View to the PM.
 */
class UserEditFormManualBinder extends
    PbFormBase<UserEditFormManual, UserEditPm> {

  @Override
  public UserEditFormManual makeView(Container parent, UserEditPm pm) {
    return new UserEditFormManual();
  }

  @Override
  protected PbBinding makeBinding(UserEditPm pm) {
    return new Binding() {
      @Override
      public void bind() {
        super.bind();
        bindAttr(view.nameLabel, view.nameText, pm.name);
        bindAttr(view.birthdayLabel, view.birthdayText, pm.birthday);
        bindAttr(view.genderLabel, view.genderCombo, pm.gender);
      }
    };
  }
  
  public static void main(String[] args) {
    UserEditPm pm = UserTestDataFactory.makeUserEditPm();
    UserEditFormManual view = new UserEditFormManual();

    (new UserEditFormManualBinder()).bind(view, pm);

    JDialog dlg = new JDialog((Frame) null, "User Edit Form");
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.setModal(true);
    dlg.setContentPane(view);
    dlg.setSize(250, 150);
    dlg.setLocationRelativeTo(null); // centered
    dlg.setVisible(true);

    System.out.println(String.format("Name: %s; Birthday: %s; Gender: %s",
        pm.name.getValue(), pm.birthday.getValue(), pm.gender.getValue()));
  }
}