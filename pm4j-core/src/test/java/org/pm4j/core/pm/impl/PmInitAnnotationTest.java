package org.pm4j.core.pm.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.annotation.PmInit;

/**
 * Test the correct functionality of the {@link PmInit} annotation.
 * 
 * @author SDOLKE
 * 
 */
public class PmInitAnnotationTest {

  private List<String> methodCallStack = new ArrayList<String>();

  /**
   * If a class overrides the onPmInit method and additionally annotates the
   * method with @OnPmInit it must be ensured, that the method is only called
   * once.
   */
  @Test
  public void testAnnotatedOnPmInit() {
    initPm(new PmObjectBase() {
      @PmInit
      protected void onPmInit() {
        methodCallStack.add("onPmInit");
      }
    });

    assertEquals(1, methodCallStack.size());
    assertEquals("onPmInit", methodCallStack.get(0));
  }

  /**
   * If two classes in a class hierarchy each got an annotated method, first the
   * method of the super class is called and then the method of the sub class is
   * called.
   */
  @Test
  public void testInitInClassHierarchy() {
    initPm(new AbstractPm() {
      @PmInit
      public void initConcretePm() {
        methodCallStack.add("initConcretePm");
      }
    });

    assertEquals(2, methodCallStack.size());
    assertEquals("initAbstractPm", methodCallStack.get(0));
    assertEquals("initConcretePm", methodCallStack.get(1));
  }

  /**
   * If a class overrides the onPmInit method and additionally declares an
   * annotated init method, then first the overridden onPmInit method and then
   * the annotated init method is called
   */
  @Test
  public void testMixedStyle() {
    initPm(new PmObjectBase() {
      @PmInit
      protected void init() {
        methodCallStack.add("annotated");
      }

      @Override
      protected void onPmInit() {
        methodCallStack.add("overridden");
      }
    });

    assertEquals(2, methodCallStack.size());
    assertEquals("overridden", methodCallStack.get(0));
    assertEquals("annotated", methodCallStack.get(1));
  }

  /**
   * If a sub class overrides an annotated super class init method it must be
   * ensured that only the method of the sub class is called.
   */
  @Test()
  public void testOverriddenInitMethod() {
    // Case 1: the sub class overrides the init method and don't annotate it.
    // This is a valid case, the method of the sub class must be called.
    initPm(new AbstractPm() {
      @Override
      public void initAbstractPm() {
        methodCallStack.add("initAbstractPm_overridden");
      }
    });
    assertEquals(1, methodCallStack.size());
    assertEquals("initAbstractPm_overridden", methodCallStack.get(0));

    methodCallStack.clear();

    // case 2: the sub class overrides the init method and additionally annotate
    // it with PmInit. This is a valid case, the method of the sub class must be called.
    initPm(new AbstractPm() {
      @PmInit
      @Override
      public void initAbstractPm() {
        methodCallStack.add("initAbstractPm_overridden");
      }
    });
    assertEquals(1, methodCallStack.size());
    assertEquals("initAbstractPm_overridden", methodCallStack.get(0));
  }

  /**
   * Annotated methods with package visibility are not allowed
   */
  @Test(expected = PmRuntimeException.class)
  public void testPackageInitMethod() {
    initPm(new PmObjectBase() {
      @PmInit
      void init() {
      }
    });
  }

  /**
   * Annotated private methods are not allowed
   */
  @Test(expected = PmRuntimeException.class)
  public void testPrivateInitMethod() {
    initPm(new PmObjectBase() {
      @PmInit
      private void init() {
      }
    });
  }

  /**
   * Static init methods are not allowed.
   */
  @Test(expected = PmRuntimeException.class)
  public void testStaticInitMethod() {
    initPm(new PmWithStaticInitMethod());
  }

  /**
   * Initialized the given PM so that the init methods get called.
   * 
   * @param pm
   */
  private void initPm(PmObjectBase pm) {
    pm.setPmParent(new PmConversationImpl());
    pm.isPmEnabled();
  }

  /**
   * An abstract PM with an annotated init method to test the initialization in
   * class hierarchies.
   * 
   * @author sdolke
   */
  private abstract class AbstractPm extends PmObjectBase {
    @PmInit
    public void initAbstractPm() {
      methodCallStack.add("initAbstractPm");
    }
  }

  /**
   * A pm with a static init method.
   * 
   * @author sdolke
   */
  private static class PmWithStaticInitMethod extends PmObjectBase {
    @PmInit
    public static void initStatic() {
    }
  }

}
