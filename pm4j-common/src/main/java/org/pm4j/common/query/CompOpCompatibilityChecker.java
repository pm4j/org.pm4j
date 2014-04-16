package org.pm4j.common.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pm4j.common.query.CompOp.ValueNeeded;

/**
 * A configurable compare operator applicability checker.
 * <p>
 * The concrete applicability of values and types may depends on the kind of used constraint evaluation.
 *
 * @author Olaf Boede
 */
public class CompOpCompatibilityChecker {

  private Map<CompOp, Set<Class<?>>> compOpToValueClassesMap = new HashMap<CompOp, Set<Class<?>>>();

  /**
   * @param compOp The {@link CompOp} to use. Should not be <code>null</code>.
   * @param compValue The value used in combination with the given {@link CompOp}. May be <code>null</code>.
   * @return <code>true</code> to the given value matches the given {@link CompOp}.
   */
  public boolean isCompOpApplicableForValue(CompOp compOp, Object compValue) {
    if (compValue == null) {
      return compOp.getValueNeeded() == ValueNeeded.NO;
    } else {
      return compOp.getValueNeeded() != ValueNeeded.NO &&
             isCompOpApplicableForValueClass(compOp, compValue.getClass());
    }
  }

  /**
   * @param compOp The {@link CompOp} to use. Should not be <code>null</code>.
   * @param valueClass The class to be check. Should not be <code>null</code>.
   * @return <code>true</code> if the values of the given class may be used for the given {@link CompOp}.
   */
  public boolean isCompOpApplicableForValueClass(CompOp compOp, Class<?> valueClass) {
    Set<Class<?>> valueClasses = compOpToValueClassesMap.get(compOp);
    if (valueClasses != null) {
      for (Class<?> c : valueClasses) {
        if (c.isAssignableFrom(valueClass)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Registers a matching {@link CompOp} value class pair.
   *
   * @param compOp The {@link CompOp}. Should not be <code>null</code>.
   * @param valueClass The value class. Should not be <code>null</code>.
   */
  public synchronized void registerCompOp(CompOp compOp, Class<?> valueClass) {
    Set<Class<?>> valueClasses = compOpToValueClassesMap.get(compOp);
    if (valueClasses == null) {
      valueClasses = new HashSet<Class<?>>();
      compOpToValueClassesMap.put(compOp, valueClasses);
    }
    if (!valueClasses.contains(valueClass)) {
      valueClasses.add(valueClass);
    }
  }

}
