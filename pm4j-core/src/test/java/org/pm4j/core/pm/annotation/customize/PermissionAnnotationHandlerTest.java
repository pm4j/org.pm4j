package org.pm4j.core.pm.annotation.customize;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.customize.PermissionAnnotationHandler;
import org.pm4j.core.pm.annotation.customize.PmAnnotationApi;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmUtil;

public class PermissionAnnotationHandlerTest {

  public static class MyPermissionAnnotatedPm extends PmElementImpl {

    /** Visible for all, but only editable for authors. */
    @MyPermissionCfg(editableFor=UserRole.AUTHOR)
    public final PmAttrString name = new PmAttrStringImpl(this);

    /** Visible for all, but only editable for authors. */
    @MyPermissionCfg(visibleFor=UserRole.AUTHOR)
    public final PmAttrString authorComment = new PmAttrStringImpl(this);

    /** Visible for all, but only editable for readers. */
    @MyPermissionCfg(editableFor=UserRole.READER)
    public final PmAttrString readerComment = new PmAttrStringImpl(this);

    public MyPermissionAnnotatedPm(PmObject pmParent) {
      super(pmParent);
    }
  }

  public static class MyConversationPm extends PmConversationImpl implements UserRoleProvider {
    private UserRole userRole = UserRole.READER;

    public MyConversationPm(UserRole userRole) {
      this.userRole = userRole;
    }

    @Override
    public UserRole getUserRole() {
      return userRole;
    }
  }

  @Before
  public void setUp() {
    PmAnnotationApi.addPermissionAnnotationHandler(MyPermissionCfg.class, new MyHandler());
  }

  @Test
  public void testReaderPermissions() {
    MyPermissionAnnotatedPm pm = new MyPermissionAnnotatedPm(new MyConversationPm(UserRole.READER));

    assertEquals(false, pm.name.isPmEnabled());
    assertEquals(true, pm.name.isPmVisible());

    assertEquals(false, pm.authorComment.isPmVisible());

    assertEquals(true, pm.readerComment.isPmEnabled());
  }

  @Test
  public void testAuthorPermissions() {
    MyPermissionAnnotatedPm pm = new MyPermissionAnnotatedPm(new MyConversationPm(UserRole.AUTHOR));

    assertEquals(true, pm.name.isPmEnabled());
    assertEquals(true, pm.name.isPmVisible());

    assertEquals(true, pm.authorComment.isPmVisible());

    assertEquals(false, pm.readerComment.isPmEnabled());
  }


  public enum UserRole {
    AUTHOR, READER, UNKNOWN
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  public @interface MyPermissionCfg {
    UserRole editableFor() default UserRole.UNKNOWN;
    UserRole visibleFor() default UserRole.UNKNOWN;
    UserRole readonlyFor() default UserRole.UNKNOWN;
  }

  interface UserRoleProvider {
    UserRole getUserRole();
  }


  class MyHandler implements PermissionAnnotationHandler<MyPermissionCfg> {

    @Override
    public boolean isEnabled(PmObject pm, MyPermissionCfg annotation) {
      return doesUserRoleMatch(pm, annotation.editableFor());
    }

    @Override
    public boolean isVisible(PmObject pm, MyPermissionCfg annotation) {
      return doesUserRoleMatch(pm, annotation.visibleFor());
    }

    @Override
    public boolean isReadonly(PmObject pm, MyPermissionCfg annotation) {
      UserRole role = annotation.readonlyFor();
      return (role == UserRole.UNKNOWN) ? false : doesUserRoleMatch(pm, role);
    }

    private boolean doesUserRoleMatch(PmObject pmCtxt, UserRole userRole) {
      if (userRole == UserRole.UNKNOWN) {
        return true;
      } else {
        UserRoleProvider userRoleProvider = PmUtil.getPmParentOfType(pmCtxt, UserRoleProvider.class);
        UserRole realUserRole = userRoleProvider.getUserRole();
        return realUserRole == userRole;
      }
    }
  }



}
