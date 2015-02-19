

package org.pm4j.common.expr;

import org.pm4j.common.expr.NameWithModifier.Modifier;

public interface OptionalExpression extends Expression {

  boolean hasNameModifier(Modifier nameModifier);
}
