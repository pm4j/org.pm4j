package org.pm4j.core.pm.impl.pathresolver;

import static org.pm4j.common.expr.Expression.SyntaxVersion.VERSION_1;

import org.pm4j.common.expr.ExprExecCtxt;
import org.pm4j.common.expr.Expression;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.expr.PathExpressionChain;
import org.pm4j.core.pm.impl.expr.PmExprExecCtxt;

public class PathResolverSyntaxVersion1Test extends PathResolverTest {

  /**
   * Reading a named object from PmConversation. Notice the MISSING '#' sign.
   * The default is the compatibility mode.
   */
  public void testCompatiblityStyleReadFromPmConversationObject() {
    PmConversation pmConversation = new PmConversationImpl();
    pmConversation.setPmNamedObject("myProp", Pojo.make("head", "subName", "subSubName"));
    Expression expr = PathExpressionChain.parse("myProp.sub.sub.name", VERSION_1);
    assertEquals("subSubName", expr.exec(new PmExprExecCtxt(pmConversation)));
  }

  /**
   * Check optional not existing field access in compatibility mode.
   */
  public void testCompatibleStyleOptionalField() {
    Pojo p = Pojo.make("head", "subName");
    Expression expr = PathExpressionChain.parse("sub.(o)notExistingField", VERSION_1);
    assertNull(expr.exec(new ExprExecCtxt(p)));
  }

}
