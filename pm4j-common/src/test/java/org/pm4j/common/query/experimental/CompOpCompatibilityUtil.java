package org.pm4j.common.query.experimental;

import java.util.Collection;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpContains;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGe;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIn;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLe;
import org.pm4j.common.query.CompOpLike;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotContains;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpNotNull;
import org.pm4j.common.query.CompOpStartsWith;

/**
 * Utilities for the abstract query functionality.
 *
 * @author Olaf Boede
 */
public class CompOpCompatibilityUtil {

  /**
   * Registers the default {@link CompOp} to value type matches that most
   * evaluators may support.
   *
   * @param checker The checker to register the matches for.
   * @return The checker again for fluent programming style.
   */
  public static CompOpCompatibilityChecker registerDefaultCompOpValueMatches(CompOpCompatibilityChecker checker) {
    checker.registerCompOp(CompOpContains.class, String.class);
    checker.registerCompOp(CompOpEquals.class, Object.class);
    checker.registerCompOp(CompOpGe.class, Comparable.class);
    checker.registerCompOp(CompOpGt.class, Comparable.class);
    checker.registerCompOp(CompOpIn.class, Collection.class);
    checker.registerCompOp(CompOpIsNull.class, Object.class);
    checker.registerCompOp(CompOpLe.class, Comparable.class);
    checker.registerCompOp(CompOpLike.class, String.class);
    checker.registerCompOp(CompOpLt.class, Comparable.class);
    checker.registerCompOp(CompOpNotContains.class, String.class);
    checker.registerCompOp(CompOpNotEquals.class, Object.class);
    checker.registerCompOp(CompOpNotNull.class, Object.class);
    checker.registerCompOp(CompOpStartsWith.class, String.class);
    return checker;
  }

}
