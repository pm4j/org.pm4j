package org.pm4j.core.pm;

import junit.framework.TestCase;

import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.navi.NaviHistory;

public class PresentationModelPmPropertyTest extends TestCase {

  // -- Domain context data --

  public static class MyCtxtData {
    public String s1 = "s1";
    public String s2 = "s2";
    public String getS1() {   return s1;   }
    public String getS2() {   return s2;   }
  }

  // -- A presentation model that uses domain context data --

  public final class MySessionPm extends PmConversationImpl {
    /**
     * Provides the context data if it is not yet initialized.<br>
     * Keeps a session-scoped reference to it in a session property. <br>
     * The next call to {@link PmObject#findPmProperty(String)} will find
     * it by name.
     * <p>
     * In case of existing PM history or request object, this method could store
     * the object in conversation, navigation or request scope.
     * <p>
     * See also:
     * <ul>
     *   <li>{@link NaviHistory#setNaviScopeProperty(String, Object)}</li>
     *   <li>{@link NaviHistory#setConversationProperty(String, java.io.Serializable)</li>
     *   <li>{@link PmViewTechnologyConnector#setRequestAttribute(String, Object)</li>
     * </ul>
     */
    @Override
    protected void handleNamedPmObjectNotFound(String name) {
      if (name.equals("myCtxtData")) {
        setPmNamedObject("myCtxtData", new MyCtxtData());
      }
      else {
        super.handleNamedPmObjectNotFound(name);
      }
    }
  }

  public final class MyPm extends PmElementImpl {

    public MyPm(PmObject pmParent) {
      super(pmParent);
    }

    @PmInject(value="myCtxtData.s1")
    private String myInjectedS1;

    @PmInject(value="nonExistingPropertyRef", nullAllowed=true)
    private String myNullInjectedField;

  }

  // -- Tests --

  private MySessionPm mySessionPm;
  private MyPm myPm;

  @Override
  protected void setUp() throws Exception {
    mySessionPm = new MySessionPm();
    myPm = new MyPm(mySessionPm);
  }

  public void testReadInjectedPmProperty() {
    // XXX olaf: we have to ensure that the PM initialization was done.
    //           Otherwise the PM injection was not yet done...
    myPm.getPmTitle();
    assertEquals("s1", myPm.myInjectedS1);
    assertEquals(null, myPm.myNullInjectedField);
  }

  public void testGetPmProperty() {
    assertEquals("s1", PmExpressionApi.getByExpression(myPm, "myCtxtData.s1"));
    assertEquals("s2", PmExpressionApi.getByExpression(myPm, "myCtxtData.s2"));
  }

}
