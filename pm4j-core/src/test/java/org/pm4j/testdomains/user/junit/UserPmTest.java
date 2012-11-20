package org.pm4j.testdomains.user.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import org.junit.Ignore;
import org.junit.Test;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.api.PmFactoryApi;
import org.pm4j.core.pm.api.PmMessageUtil;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.testdomains.user.AdminSession;
import org.pm4j.testdomains.user.Domain;
import org.pm4j.testdomains.user.DomainPm;
import org.pm4j.testdomains.user.User;
import org.pm4j.testdomains.user.User.Salutation;
import org.pm4j.testdomains.user.UserPm;

public class UserPmTest {

  @Test
  @Ignore("olaf: Factory delegation to the conversation is no longer supported. Rewrite this test.")
  public void testUserModel() {
    PmConversation pmConversation = new AdminSession(null);
    Domain domain = new Domain("testDomain");
    /*User user1 =*/ domain.addToUsers(new User("Berta"));
    User user2 = domain.addToUsers(new User("Willi"));

    UserPm userPm = PmFactoryApi.getPmForBean(pmConversation, user2);

    assertEquals("Willi", userPm.getPmTitle());

    assertEquals("Willi", userPm.name.getValue());
    assertNull(userPm.description.getValue());
    assertEquals(1000, userPm.description.getMaxLen());

    assertEquals("An additional null-option because the current value is null.", "[---, Herr, Frau]", userPm.salutation.getOptionSet().getOptions().toString());
    userPm.salutation.setValue(Salutation.MR);
    assertEquals("The null-option disappears if the value is not null.", "[Herr, Frau]", userPm.salutation.getOptionSet().getOptions().toString());
    assertEquals(Salutation.MR, userPm.salutation.getValue());
    assertEquals("Herr Willi", userPm.fullName.getValue());

    userPm.salutation.setValueAsString("MRS");
    assertEquals(Salutation.MRS, userPm.salutation.getValue());
    assertEquals("Anrede", userPm.salutation.getPmTitle());
    assertEquals("Frau", userPm.salutation.getOptionSet().getOptions().get(1).getPmTitle());

    assertEquals("[---, Berta, Willi]", userPm.associate.getOptionSet().getOptions().toString());
    PmOption bertaOption = userPm.associate.getOptionSet().getOptions().get(1);
    assertEquals("Berta", bertaOption.getPmTitle());
    userPm.associate.setValueAsString(bertaOption.getIdAsString());
    assertEquals("Berta", userPm.associate.getValue().name.getValue());

    DomainPm domainPm = PmFactoryApi.getPmForBean(pmConversation, domain);
    List<UserPm> users = domainPm.users.getValue();
    assertEquals(2, users.size());
    assertEquals("Berta", users.get(0).name.getValue());
    assertEquals("Willi", users.get(1).name.getValue());

    assertTrue(userPm == users.get(1));

    UserPm userFelix = PmFactoryApi.getPmForBean(pmConversation, new User("Felix"));

    domainPm.users.add(userFelix);
    assertEquals(3, domainPm.users.getValue().size());
  }

  @Test
  @Ignore("olaf: Factory delegation to the conversation is no longer supported. Rewrite this test.")
  public void testUserModelWithDefaultSession() {
    Domain domain = new Domain("testDomain");
    /*User user1 =*/ domain.addToUsers(new User("Berta"));
    User user2 = domain.addToUsers(new User("Willi"));

    PmConversation pmConversation = new PmConversationImpl(UserPm.class, DomainPm.class);

    pmConversation.setPmLocale(Locale.GERMAN);
    UserPm userPm = PmFactoryApi.getPmForBean(pmConversation, user2);
    // new UserPm(pmConversation, user2);

    assertEquals("Willi", userPm.getPmTitle());

    assertEquals("Willi", userPm.name.getValue());
    assertNull(userPm.description.getValue());
    assertEquals(1000, userPm.description.getMaxLen());

    assertEquals("[---, Herr, Frau]", userPm.salutation.getOptionSet().getOptions().toString());
    userPm.salutation.setValue(Salutation.MR);
    assertEquals(Salutation.MR, userPm.salutation.getValue());
    userPm.salutation.setValueAsString("MRS");
    assertEquals(Salutation.MRS, userPm.salutation.getValue());
    assertEquals("Anrede", userPm.salutation.getPmTitle());
    assertEquals("Frau", userPm.salutation.getOptionSet().getOptions().get(1).getPmTitle());

    assertEquals("[---, Berta, Willi]", userPm.associate.getOptionSet().getOptions().toString());
    PmOption bertaOption = userPm.associate.getOptionSet().getOptions().get(1);
    assertEquals("Berta", bertaOption.getPmTitle());
    userPm.associate.setValueAsString(bertaOption.getIdAsString());
    assertEquals("Berta", userPm.associate.getValue().name.getValue());

    DomainPm domainPm = PmFactoryApi.getPmForBean(pmConversation, domain);
    assertEquals(2, domainPm.users.getValue().size());
    assertEquals("Berta", domainPm.users.getValue().get(0).name.getValue());
    assertEquals("Willi", domainPm.users.getValue().get(1).name.getValue());

    assertTrue(userPm == domainPm.users.getValue().get(1));

    UserPm userFelix = PmFactoryApi.getPmForBean(pmConversation, new User("Felix"));

    domainPm.users.add(userFelix);
    assertEquals(3, domainPm.users.getValue().size());
  }

  @Test
  public void testPmIdentityWithDefaultSession() {
    Domain domain = new Domain("testDomain");
    User user1 = domain.addToUsers(new User("Willi"));
    User user2 = domain.addToUsers(new User("Berta"));
    user1.setAssociate(user2);

    PmConversation session = new PmConversationImpl(UserPm.class);
    UserPm userPm = PmFactoryApi.getPmForBean(session, user1);

    assertEquals("Willi", userPm.getPmTitle());
    assertEquals("Willi", userPm.name.getValue());
    assertEquals(user2, userPm.associate.getValueAsBean());
  }

  @Test
  public void testValidation() {
    User user = new User("Willi");
    PmConversation session = new PmConversationImpl(UserPm.class);
    session.setBufferedPmValueMode(true);
    UserPm userPm = PmFactoryApi.getPmForBean(session, user);

    assertTrue(userPm.name.isPmValid());

    userPm.name.setValue(null);
    userPm.cmdCommitChanges.doIt();
    assertFalse(userPm.name.isPmValid());
    assertEquals(null, userPm.name.getValue());
    assertEquals("Willi", user.getName());
    PmMessageUtil.clearSubTreeMessages(userPm);

    userPm.name.setValue("");
    userPm.cmdCommitChanges.doIt();
    assertFalse(userPm.name.isPmValid());
    // FIXME: should return the invalid value...
//    assertEquals("", userPm.name.getValue());
    assertEquals("Willi", user.getName());
    PmMessageUtil.clearSubTreeMessages(userPm);

    userPm.name.setValue("ab");
    userPm.cmdCommitChanges.doIt();
    assertTrue(userPm.name.isPmValid());
    assertEquals("ab", userPm.name.getValue());
    assertEquals("ab", user.getName());

    userPm.name.setValue("ab1234567890");
    userPm.cmdCommitChanges.doIt();
    assertFalse(userPm.name.isPmValid());
    assertEquals("ab1234567890", userPm.name.getValue());
    assertEquals("ab", user.getName());
    userPm.clearPmInvalidValues();

    // FIXME: the invalid value should have been cleaned...
    //assertEquals("ab", userPm.name.getValue());

    userPm.name.setValue("ab1234567890");
    userPm.cmdCommitChanges.doIt();
    List<PmMessage> messages = userPm.getPmConversation().getPmMessages();
    assertEquals(1, messages.size());
    assertEquals(userPm.name, messages.get(0).getPm());

    PmMessageUtil.clearSubTreeMessages(userPm.getPmConversation());
    // FIXME: the invalid value should have been cleaned...
    // assertEquals("ab", userPm.name.getValue());
    assertEquals(0, userPm.getPmConversation().getPmMessages().size());

    for (Type t : userPm.getClass().getGenericInterfaces()) {
      System.out.println("genType: " + t.toString());
      Class<?> c = t.getClass();
      System.out.println("genType class: " + c.toString());
      ParameterizedType pt = (ParameterizedType)t;
      for (Type ta : pt.getActualTypeArguments()) {
        System.out.println("type arg: " + ta.toString());
      }
    }

    Class<?> beanCls = findBeanClass(userPm.getClass());
    System.out.println("bean class: " + beanCls);
    assertEquals(User.class, beanCls);

// -------
    System.out.println("--- domain ....");

    Class<?> cl = DomainPm.class;
    while (cl != Object.class && cl != Class.class) {
      for (Type t : cl.getGenericInterfaces()) {
        System.out.println("genType: " + t.toString());
        Class<?> c = t.getClass();
        System.out.println("genType class: " + c.toString());
        if (t instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType)t;
          for (Type ta : pt.getActualTypeArguments()) {
            System.out.println("type arg: " + ta.toString());
          }
        }
        else {
          System.out.println(" other type: " + t.getClass());
        }
      }

      System.out.println(" generic super: " + cl.getGenericSuperclass() + " von " + cl);
      Type genSuper = cl.getGenericSuperclass();
      if (genSuper instanceof Class) {
        for (Type t : ((Class<?>)genSuper).getGenericInterfaces()) {
          System.out.println("genType: " + t.toString());
          Class<?> c = t.getClass();
          System.out.println("genType class: " + c.toString());
          if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)t;
            for (Type ta : pt.getActualTypeArguments()) {
              System.out.println("type arg: " + ta.toString());
            }
          }
          else {
            System.out.println(" other type: " + t.getClass());
          }
        }
      }

      cl = cl.getSuperclass();
    }

    beanCls = findBeanClass(DomainPm.class);
    System.out.println("bean class: " + beanCls);
//    assertEquals(User.class, beanCls);
}

  Class<?> findBeanClass(Class<?> beanClass) {
    Type pmIf = null;
    for (Type t : beanClass.getGenericInterfaces()) {
      if (t instanceof ParameterizedType) {
        ParameterizedType pt = (ParameterizedType)t;
        Type rt = pt.getRawType();
        if (rt instanceof Class<?>
        && PmBean.class.isAssignableFrom((Class<?>)rt)
            ) {
          pmIf = t;
          break;
        }
      }
    }

    if (pmIf == null) {
      return null;
    }

    Type[] typeArgs = ((ParameterizedType)pmIf).getActualTypeArguments();
    if (typeArgs.length != 1) {
      return null;
    }

    return (Class<?>) typeArgs[0];
  }
}
