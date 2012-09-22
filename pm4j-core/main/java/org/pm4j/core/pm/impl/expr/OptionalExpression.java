

package org.pm4j.core.pm.impl.expr;

import org.pm4j.core.pm.impl.expr.NameWithModifier.Modifier;

public interface OptionalExpression extends Expression {

  boolean hasNameModifier(Modifier nameModifier);
}
