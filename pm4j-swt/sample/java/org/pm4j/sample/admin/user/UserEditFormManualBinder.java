package org.pm4j.sample.admin.user;

import org.eclipse.swt.widgets.Composite;
import org.pm4j.core.sample.admin.user.UserEditPm;
import org.pm4j.core.sample.admin.user.service.UserTestDataFactory;
import org.pm4j.swt.pb.base.PbFormBase;
import org.pm4j.swt.testtools.SwtTestShell;

/**
 * Binds the View to the PM.
 */
class UserEditFormManualBinder extends PbFormBase<UserEditFormHandmade, UserEditPm> {

  @Override
  public UserEditFormHandmade makeView(Composite parent, UserEditPm pm) {
    return new UserEditFormHandmade(parent, swtStyle);
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
    SwtTestShell s = new SwtTestShell(250, 150, "User Edit Form");
    new UserEditFormManualBinder().build(s.getShell(), UserTestDataFactory.makeUserEditPm());
    s.show();
  }
  
}