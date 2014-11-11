package org.pm4j.core.pm.impl.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.hibernate.validator.util.ReflectionHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.PmInitApi;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;
import org.pm4j.core.pm.impl.pathresolver.PmExpressionPathResolver;

import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;

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
    public void setMySetterInitializedFakeProp(String value) {
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

  private static final Method mySetterInitializedNullPropSetter = getSetter(MyPm.class, "mySetterInitializedNullProp");
  private static final Method mySetterInitializedPropSetter = getSetter(MyPm.class, "mySetterInitializedProp");
  private static final Method mySetterInitializedFakeProp = getSetter(MyPm.class, "mySetterInitializedFakeProp");
  private static final Method myAlienPropSetter = getSetter(AnotherPm.class, "myAlienProp");

  @Before
  public void setUp() {
    pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myFieldInitializedNullProp", null);
    pmConversation.setPmNamedObject("myFieldInitializedProp", "[initialized directly]");
    pmConversation.setPmNamedObject("myPropWithoutGetter", "[should not appear anywhere]");
    pmConversation.setPmNamedObject("mySetterInitializedProp", "[initialized by setter]");
    myPm = new MyPm(pmConversation); // tree deleberatly not yet initilized
  }
  
  @Test
  public void withProperValuesPmIsInitializedProperly() {
    initMyPm();
    assertEquals("[initialized directly]", myPm.myFieldInitializedProp);
    assertEquals("[initialized by setter]", myPm.mySetterInitializedProp);
  }

  
  @Test
  public void validateFieldIsNullThrowsExceptionWhenFieldAlreadyContainsValue() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Can't initialize field 'myFieldInitializedNullProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.  Already has value: [faking some preexisting initialization]"));
    myPm.myFieldInitializedNullProp = "[faking some preexisting initialization]";
    DiResolverUtil.validateFieldIsNull(myPm, ReflectionHelper.getField(MyPm.class, "myFieldInitializedNullProp"));
  }
  
  @Test
  public void validateFieldIsNullThrowsExceptionWhenFieldIsNotReadable() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Can't read field 'myAlienFakeProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'."));
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
    expectedException.expectMessage(startsWith("Can't initialize setter 'setMySetterInitializedNullProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'.  Already has value: [faking some preexisting initialization]"));
    myPm.setMySetterInitializedNullProp("[faking some preexisting initialization]");
    DiResolverUtil.validateGetterReturnsNull(myPm, mySetterInitializedNullPropSetter);
  }
   
  @Test
  public void validateGetterReturnsNullThrowsExceptionWhenThereIsNoSetter() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Can't invoke getter for 'setMySetterInitializedFakeProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'."));
    DiResolverUtil.validateGetterReturnsNull(myPm, mySetterInitializedFakeProp);
  }
  
  @Test
  public void validateGetterReturnsNullDoesAcceptNullValue() {
    myPm.setMySetterInitializedNullProp(null);
    DiResolverUtil.validateGetterReturnsNull(myPm, mySetterInitializedNullPropSetter);
  }
  
 
  @Test
  public void validateValidValue() {
    DiResolverUtil.validateValidValue(myPm, true, myFieldInitializedNullPropField, null);
    DiResolverUtil.validateValidValue(myPm, false, myFieldInitializedPropField, "[some non-null value]");
    DiResolverUtil.validateValidValue(myPm, true, mySetterInitializedNullPropSetter, null);
    DiResolverUtil.validateValidValue(myPm, false, mySetterInitializedPropSetter, "[some non-null value]");
  }

  @Test
  public void validateValidValueThrowsExceptionForNullValueIfNullNotAllowed() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Found value for dependency injection of 'private java.lang.String org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm.myFieldInitializedNullProp' was null. But null value is not allowed. You may configure null-value handling using @PmInject(nullAllowed=...)."));
    DiResolverUtil.validateValidValue(myPm, false, myFieldInitializedNullPropField, null);
  }

  @Test
  public void setValueDirectActuallySetsTheValue() {
    initMyPm();
    DiResolverUtil.setValue(myPm, myFieldInitializedPropField, "[some value]");
    assertEquals("[some value]", myPm.myFieldInitializedProp);
  }
  
  @Test
  public void setValueDirectThrowsExceptionIfFieldCantBeSet() {
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Can't initialize field 'myAlienFakeProp'"));
    DiResolverUtil.setValue(myPm, myAlienFakePropField, "[some value]");
  }
  
  @Test
  public void setValueViaSetterActuallySetsTheValue() {
    initMyPm();
    DiResolverUtil.setValue(myPm, mySetterInitializedPropSetter, "[some value]");
    assertEquals("[some value]", myPm.getMySetterInitializedProp());
  }
  
  @Test
  public void setValueViaSetterThrowsExceptionIfFieldCantBeSet() {      
    expectedException.expect(PmRuntimeException.class);
    expectedException.expectMessage(startsWith("Can't invoke method 'setMyAlienProp' in class 'org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm'."));
    DiResolverUtil.setValue(myPm, myAlienPropSetter, "[some value]");
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
    expectedException.expectMessage(startsWith("Unable to resolve dependency injection reference to '#myFieldInitializedProp.someNonExistingProp' for: private java.lang.String org.pm4j.core.pm.impl.inject.DiResolverUtilTest$MyPm.myFieldInitializedProp"));
    PathResolver pr = PmExpressionPathResolver.parse("#myFieldInitializedProp.someNonExistingProp", SyntaxVersion.VERSION_2);
    DiResolverUtil.resolveValue(myPm, myFieldInitializedPropField, pr);
  }  

  
  // just a minimal version feasible for this test
  private static Method getSetter(Class<?> clazz, String property) {
    String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
    try {
      Method method = clazz.getDeclaredMethod(setterName, String.class);
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
