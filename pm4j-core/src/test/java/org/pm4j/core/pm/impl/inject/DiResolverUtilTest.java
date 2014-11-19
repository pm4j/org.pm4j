package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.hibernate.validator.util.ReflectionHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.util.reflection.PrefixUtil;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DiResolverUtilTest {

  private PmConversation pmConversation;
  private MyPm myPm;

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  public final class MyPm extends PmElementImpl {

    public MyPm(PmObject pmParent) {
      super(pmParent);
    }

    @PmInject(value="#myFieldInitializedNullProp", nullAllowed=true)
    private String myFieldInitializedNullProp;

    @PmInject(value="#myFieldInitializedProp")
    private String myFieldInitializedProp;

    public String mySetterInitializedNullProp;

    public String mySetterInitializedProp;

    private boolean mySetterInitializedPropWithoutGetter;

    public String getMySetterInitializedProp() {
      return mySetterInitializedProp;
    }

    @PmInject(value="#mySetterInitializedProp")
    public void setMySetterInitializedProp(String value) {
      mySetterInitializedProp = value;
    }

    public String getMyNullProp() {
      return mySetterInitializedNullProp;
    }

    @PmInject(value="#mySetterInitializedNullProp", nullAllowed=true)
    public void setMySetterInitializedNullProp(String value) {
      mySetterInitializedNullProp = value;
    }

    public String getMySetterInitializedNullProp() {
      return mySetterInitializedNullProp;
    }

    // there deliberately is no getter for this setter
    @PmInject(value="#mySetterInitializedPropWithoutGetter")
    public void setMySetterInitializedPropWithoutGetter(boolean value) {
      mySetterInitializedPropWithoutGetter = value;
    }

    public void setMySetterInitializedFakeProp(String value) {
    }

    public String getMySetterInitializedFakeProp() {
      throw new IllegalStateException("faking some Exception in getter");
    }
  }

  public final class AnotherPm {
    // deliberately here and not in MyPm to have a field from another class
    @SuppressWarnings("unused")
    private String myAlienFakeProp;

    public void setMyAlienProp(String value) {
    }
  }

  private static final Field myFieldInitializedNullPropField = ReflectionHelper.getField(MyPm.class, "myFieldInitializedNullProp");
  private static final Field myFieldInitializedPropField = ReflectionHelper.getField(MyPm.class, "myFieldInitializedProp");
  private static final Field myAlienFakePropField = ReflectionHelper.getField(AnotherPm.class, "myAlienFakeProp");

  private static final Method mySetterInitializedNullPropSetter = getSetter(MyPm.class, "mySetterInitializedNullProp", String.class);
  private static final Method mySetterInitializedPropSetter = getSetter(MyPm.class, "mySetterInitializedProp", String.class);
  private static final Method mySetterInitializedFakeProp = getSetter(MyPm.class, "mySetterInitializedFakeProp", String.class);
  private static final Method mySetterInitializedPropWithoutGetterSetter  = getSetter(MyPm.class, "mySetterInitializedPropWithoutGetter", boolean.class);
  private static final Method myAlienPropSetter = getSetter(AnotherPm.class, "myAlienProp", String.class);

  @Before
  public void setUp() {
    pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myFieldInitializedNullProp", null);
    pmConversation.setPmNamedObject("myFieldInitializedProp", "[initialized directly]");
    pmConversation.setPmNamedObject("myPropWithoutGetter", "[should not appear anywhere]");
    pmConversation.setPmNamedObject("mySetterInitializedProp", "[initialized by setter]");
    pmConversation.setPmNamedObject("mySetterInitializedPropWithoutGetter", true);
    myPm = new MyPm(pmConversation); // tree deliberately not yet initialized
  }

  @Test
  public void withProperValuesPmIsInitializedProperly() {
    initMyPm();
    assertEquals("[initialized directly]", myPm.myFieldInitializedProp);
    assertEquals("[initialized by setter]", myPm.mySetterInitializedProp);
    assertTrue("@PmInject boolean did not work", myPm.mySetterInitializedPropWithoutGetter);
  }


  @Test
  public void validateFieldIsNullThrowsExceptionWhenFieldAlreadyContainsValue() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't initialize field 'myFieldInitializedNullProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.  Already has value: [faking some preexisting initialization]");
    myPm.myFieldInitializedNullProp = "[faking some preexisting initialization]";
    DiResolverUtil.validateFieldIsNull(myPm, ReflectionHelper.getField(MyPm.class, "myFieldInitializedNullProp"));
  }

  @Test
  public void validateFieldIsNullThrowsExceptionWhenFieldIsNotReadable() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't read field 'myAlienFakeProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.");
    DiResolverUtil.validateFieldIsNull(myPm, ReflectionHelper.getField(AnotherPm.class, "myAlienFakeProp"));
  }

  @Test
  public void validateFieldIsNullDoesAcceptValidVales() {
    myPm.myFieldInitializedNullProp = null;
    DiResolverUtil.validateFieldIsNull(myPm, myFieldInitializedNullPropField);

    initMyPm();
    assertEquals("[initialized directly]", myPm.myFieldInitializedProp);
  }

  @Test
  public void validateGetterReturnsNullThrowsExceptionWhenGetterReturnsValue() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't perform @PmInject for 'mySetterInitializedNullProp'. It already has value: [faking some preexisting initialization] - Exception context: Class: 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm' PM: 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'");
    myPm.setMySetterInitializedNullProp("[faking some preexisting initialization]");
    DiResolverUtil.validateGetterReturnsNull(myPm, PrefixUtil.findGetterForSetter(mySetterInitializedNullPropSetter));
  }

  @Test
  public void validateGetterReturnsNullThrowsExceptionWhenGetterCantBeInvoked() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't invoke getter 'getMySetterInitializedFakeProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.");
    DiResolverUtil.validateGetterReturnsNull(myPm, PrefixUtil.findGetterForSetter(mySetterInitializedFakeProp));
  }

  @Test
  public void validateGetterReturnsNullDoesAcceptNullValue() {
    myPm.setMySetterInitializedNullProp(null);
    DiResolverUtil.validateGetterReturnsNull(myPm, PrefixUtil.findGetterForSetter(mySetterInitializedNullPropSetter));
  }

  @Test
  public void validateGetterReturnsNullDoesDoesNotThrowExceptionWhenThereIsNoGetter() {
    DiResolverUtil.validateGetterReturnsNull(myPm, PrefixUtil.findGetterForSetter(mySetterInitializedPropWithoutGetterSetter));
  }



  @Test
  public void setValueWithValidValueActuallySetsTheValue() {
    DiResolverUtil.setValue(myPm, myFieldInitializedNullPropField, true, null);
    assertNull(myPm.myFieldInitializedNullProp, null);

    DiResolverUtil.setValue(myPm, myFieldInitializedPropField, false, "[some non-null value]");
    assertEquals("[some non-null value]", myPm.myFieldInitializedProp);

    DiResolverUtil.setValue(myPm, mySetterInitializedNullPropSetter, true, null);
    assertNull(myPm.mySetterInitializedNullProp);

    DiResolverUtil.setValue(myPm, mySetterInitializedPropSetter, false, "[some non-null value]");
    assertEquals("[some non-null value]", myPm.mySetterInitializedProp);
  }

  @Test
  public void setValueWithInvalidValueThrowsExceptionForNullValueIfNullNotAllowed() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Found value for dependency injection of 'private java.lang.String org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm.myFieldInitializedNullProp' was null. But null value is not allowed. You may configure null-value handling using @PmInject(nullAllowed=...).");
    DiResolverUtil.setValue(myPm, myFieldInitializedNullPropField, false, null);
  }

  @Test
  public void setValueDirectThrowsExceptionIfFieldCantBeSet() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't initialize field 'myAlienFakeProp'");
    DiResolverUtil.setValue(myPm, myAlienFakePropField, false, "[some value]");
  }

  @Test
  public void setValueViaSetterThrowsExceptionIfFieldCantBeSet() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Can't invoke method 'setMyAlienProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.");
    DiResolverUtil.setValue(myPm, myAlienPropSetter, false, "[some value]");
  }

  @Test
  public void resolveValue() {
    PathResolver pr = PmExpressionPathResolver.parse("#myFieldInitializedProp", SyntaxVersion.VERSION_2);
    Object value = DiResolverUtil.resolveValue(myPm, myFieldInitializedPropField, pr);
    assertEquals("[initialized directly]", value);
  }

  @Test
  public void resolveValueThrowsPmRuntimeExceptionIfPathNotExists() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage("Unable to resolve dependency injection reference to '#myFieldInitializedProp.someNonExistingProp' for: private java.lang.String org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm.myFieldInitializedProp");
    PathResolver pr = PmExpressionPathResolver.parse("#myFieldInitializedProp.someNonExistingProp", SyntaxVersion.VERSION_2);
    DiResolverUtil.resolveValue(myPm, myFieldInitializedPropField, pr);
  }


  // just a simple implementation for this test
  private static Method getSetter(Class<?> clazz, String property, Class<?> type) {
    String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
    try {
      Method method = clazz.getDeclaredMethod(setterName, type);
      return method;
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("no setter found for property '" + property + "'", e);
    }
  }

  // this is not in @Before to be able to test different scenarios
  private void initMyPm() {
    myPm = PmInitApi.ensurePmSubTreeInitialization(new MyPm(pmConversation));
  }
}
