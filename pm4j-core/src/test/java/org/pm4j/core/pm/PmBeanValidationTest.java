package org.pm4j.core.pm;

import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ScriptAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.annotation.PmBeanCfg;
import org.pm4j.core.pm.annotation.PmBoolean;
import org.pm4j.core.pm.annotation.PmValidationCfg;
import org.pm4j.core.pm.api.PmValidationApi;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmBeanImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementBase;

/**
 * Tests JSR-303 bean validation support.
 *
 * @author olaf boede
 */
public class PmBeanValidationTest {

  @ScriptAssert(lang = "javascript", script = "_this.i < _this.j")
  public static class MyBean {

    @Size(max=4)
    public String s;
    @NotNull
    public Integer i;
    @NotNull
    public Integer j;

    public MyBean(String s, Integer i, Integer j) {
      this.s = s;
      this.i = i;
      this.j = j;
    }
  }

  @PmBeanCfg(beanClass=MyBean.class)
  public static class MyBeanPm extends PmBeanImpl<MyBean> {
    public final PmAttrString s = new PmAttrStringImpl(this);
    public final PmAttrInteger i = new PmAttrIntegerImpl(this);
    public final PmAttrInteger j = new PmAttrIntegerImpl(this);
    public final PmCommand cmdTriggeringValidation = new PmCommandImpl(this);


    public MyBeanPm(PmObject pmParent, MyBean b) {
      super(pmParent, b);
    }
  }

  @PmValidationCfg(useJavaxValidationForBeans=PmBoolean.FALSE)
  public static class PmWithoutBeanValidation extends PmElementBase  {
    public final MyBeanPm beanPm = new MyBeanPm(this, null);
    public PmWithoutBeanValidation(PmObject pmParent) { super(pmParent); }
  }

  private PmConversation pmConversation;
  private MyBeanPm myBeanPm;
  // TODO olaf: check how to configure the validator language.
  private Locale systemDefaultLocale = Locale.getDefault();


  @Before
  public void setUp() {
    Locale.setDefault(Locale.ENGLISH);
    pmConversation = new PmConversationImpl();
    myBeanPm = new MyBeanPm(pmConversation, null);
  }

  @After
  public void tearDown() {
    Locale.setDefault(systemDefaultLocale);
  }

  @Test
  public void testAttributeValidationForEmptyBean() {
    myBeanPm.setPmBean(new MyBean(null, null, null));
    Assert.assertEquals("The @Size.max constraint is read from the bean restriction.", 4, myBeanPm.s.getMaxLen());

    myBeanPm.pmValidate();
    Assert.assertTrue(myBeanPm.s.isPmValid());
    Assert.assertFalse(myBeanPm.i.isPmValid());
    Assert.assertFalse(myBeanPm.j.isPmValid());
  }

  @Test
  public void testAttributeValidationForEmptyBeanTriggeredByCommand() {
    myBeanPm.setPmBean(new MyBean(null, null, null));
    Assert.assertEquals("The @Size.max constraint is read from the bean restriction.", 4, myBeanPm.s.getMaxLen());

    myBeanPm.cmdTriggeringValidation.doIt();
    Assert.assertTrue(myBeanPm.s.isPmValid());
    Assert.assertFalse(myBeanPm.i.isPmValid());
    Assert.assertFalse(myBeanPm.j.isPmValid());
  }

  @Test
  public void testValidationOfBeanTriggeredByPmBean() {
    myBeanPm.setPmBean(new MyBean("hi", 2, 1));

    myBeanPm.pmValidate();
    Assert.assertFalse(myBeanPm.isPmValid());

  }


  @Test
  public void testValidationOfBeanDirectly() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    MyBean b = new MyBean("hi", 1, 2);

    Set<ConstraintViolation<MyBean>> violations = validator.validate(b);
    Assert.assertEquals(0, violations.size());

    b = new MyBean("hi", 2, 1);
    violations = validator.validate(b);
    Assert.assertEquals(1, violations.size());
    Assert.assertEquals("script expression \"_this.i < _this.j\" didn't evaluate to true", violations.iterator().next().getMessage());
  }

  @Test
  public void testBeanValidationBasedFieldValidationNotSwitchedOffByAnnotation() {
    PmWithoutBeanValidation pm = new PmWithoutBeanValidation(pmConversation);
    pm.beanPm.setPmBean(new MyBean("abcdefgh", null, null));
    PmValidationApi.validateSubTree(pm);
    Assert.assertEquals(3, pmConversation.getPmMessages().size());
  }

  @Test
  public void testBeanValidationSwitchedOffByAnnotation() {
    PmWithoutBeanValidation pm = new PmWithoutBeanValidation(pmConversation);
    pm.beanPm.setPmBean(new MyBean("hi", 2, 1));
    PmValidationApi.validateSubTree(pm);
    Assert.assertEquals(0, pmConversation.getPmMessages().size());
  }
}
