package org.pm4j.core.sample.admin.user;

import org.pm4j.core.pm.PmAttrPmList;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.annotation.*;
import org.pm4j.core.pm.impl.PmAttrPmListImpl;
import org.pm4j.core.pm.impl.PmBeanBase;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmObjectBase;
import org.pm4j.core.pm.impl.commands.PmCommandNaviBack;
import org.pm4j.core.sample.admin.AdminPages;
import org.pm4j.core.sample.admin.user.service.User;
import org.pm4j.core.sample.admin.user.service.UserService;

/** PM for the page that shows the list of existing users. */
public class UserListPm extends PmObjectBase {

  /**
   * The list of user items to show.<br>
   * {@link PmAttrCfg#valuePath()} defines the set of users to show.<br>
   * {@link PmFactoryCfg#beanPmClasses()} defines the kind of PM to be used
   * for the list items.
   */
  @PmAttrCfg(valuePath="#userService.userList")
  @PmFactoryCfg(beanPmClasses=Item.class)
  public final PmAttrPmList<Item> items = new PmAttrPmListImpl<Item, User>(this);

  public final PmCommand cmdNewUser = new PmCommandImpl(this, AdminPages.USER_EDIT_PAGE);

  @PmTitleCfg(resKey="cmd.cancel")
  public final PmCommand cmdCancel = new PmCommandNaviBack(this);

  /** PM for the list items. */
  @PmBeanCfg(beanClass=User.class)
  public static class Item extends PmBeanBase<User> {

    public final PmCommand cmdEdit = new PmCommandImpl(this) {
      @Override protected String getPmTitleImpl() {
        return getPmBean().getName();
      }
      @Override protected void doItImpl() {
        navigateTo(AdminPages.makeUserEditLink(getPmBean()));
      }
    };

    @PmTitleCfg(resKey="cmd.delete")
    public final PmCommand cmdDelete = new PmCommandImpl(this) {
      @PmInject private UserService userService;
      @Override protected void doItImpl() {
        userService.deleteUser(getPmBean());
      }
    };

    // -- Getter --
    public PmCommand getCmdEdit() { return cmdEdit; }
    public PmCommand getCmdDelete() { return cmdDelete; }
  }

  // -- Getter --
  public PmAttrPmList<Item> getItems() { return items; }
  public PmCommand getCmdNewUser() { return cmdNewUser; }
  public PmCommand getCmdCancel() { return cmdCancel; };
}
