package org.pm4j.core.sample.admin.user;

import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmElement;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrStringCfg;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmFactoryCfg;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.annotation.PmTitleCfg;
import org.pm4j.core.pm.impl.PmAttrDateImpl;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.commands.PmCommandNaviBack;
import org.pm4j.core.sample.admin.user.service.Address;
import org.pm4j.core.sample.admin.user.service.User;
import org.pm4j.core.sample.admin.user.service.UserService;
import org.pm4j.core.sample.admin.user.service.User.Gender;

/**
 * PM for the User edit page.
 * <p>
 * Uses the base class {@link PmBeanBase} which supports synchronization with the
 * state of the pojo behind this PM.
 */
@PmBeanCfg(beanClass=User.class, autoCreateBean=true)
@PmTitleCfg(resKeyBase="userPm")
public class UserEditPm extends PmBeanBase<User> {

  /** Default ctor for autoCreateBean functionality (new user). */
  public UserEditPm() {
  }

  public UserEditPm(PmObject pmParent, User user) {
    super(pmParent, user);
  }

  @PmAttrCfg(required=true) @PmAttrStringCfg(maxLen=5)
  public final PmAttrString       name     = new PmAttrStringImpl(this);

  @PmAttrCfg(required=true)
  public final PmAttrDate         birthday = new PmAttrDateImpl(this);

  public final PmAttrEnum<Gender> gender   = new PmAttrEnumImpl<Gender>(this, Gender.class);

  @PmFactoryCfg(beanPmClasses=AddressListItemPm.class)
  public final PmAttrPmList<AddressListItemPm> addresses = new PmAttrPmListImpl<AddressListItemPm, Address>(this);

  @PmTitleCfg(resKey="cmd.save")
  public final PmCommand cmdSave = new PmCommandImpl(this) {
    @Override protected void doItImpl() {
      userService.saveUser(getPmBean());
      navigateBack();
    }
  };

  @PmCommandCfg(requiresValidValues=false)
  @PmTitleCfg(resKey="cmd.cancel")
  public final PmCommand cmdCancel = new PmCommandNaviBack(this);

  /** The items to show in the list of addresses. */
  public static class AddressListItemPm extends AddressPm {

    public final PmCommand cmdDeleteAddr = new PmCommandImpl(this) {
      @Override protected void doItImpl() {
        UserEditPm userEditPm = PmUtil.getPmParentOfType(this, UserEditPm.class);
        userEditPm.getPmBean().getAddresses().remove(getPmBean());
      }
    };

    // -- Getter --
    public PmCommand getCmdDeleteAddr() { return cmdDeleteAddr; }
  }

  /** Dependency injected {@link UserService} reference.  */
  @PmInject private UserService userService;

  /**
   * Loads the {@link User} that was referenced by name by the command that
   * navigates to this page.<br>
   * If no user name was provided, a new transient user instance will be edited
   * on thie current UI page.
   */
  @Override
  protected User getPmBeanImpl() {
    String name = getPmConversation().getPmNaviHistory().getNaviScopeProperty("userName");
    User user = name != null
            ? userService.findUserByName(name)
            : null;
    return user != null
            ? user
            : new User();
  }

  @Override
  protected String getPmTitleImpl() {
    User user = getPmBean();
    return user.getId() != null
             ? localize("userEditPm", user.getName())
             : localize("userEditPm_newUser");
  }

  // -- Getter --
  public PmAttrString getName()         { return name; }
  public PmAttrDate getBirthday()       { return birthday; }
  public PmAttrEnum<Gender> getGender() { return gender; }
  public PmCommand getCmdCancel()       { return cmdCancel; }
  public PmCommand getCmdSave()         { return cmdSave; }
}
