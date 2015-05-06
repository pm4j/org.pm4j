package org.pm4j.common.query.experimental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pm4j.common.query.CompOp;

/**
 * A configurable compare operator applicability checker.
 * <p>
 * The concrete applicability of values and types may depends on the kind of used constraint evaluation.
 *
 * @author Olaf Boede
 */
public class CompOpCompatibilityChecker {

  private Map<Class<CompOp>, Set<Class<?>>> compOpToValueClassesMap = new LinkedHashMap<Class<CompOp>, Set<Class<?>>>();
  private Map<Class<?>, Set<Class<CompOp>>> valueClassToCompOpsMap = new HashMap<Class<?>, Set<Class<CompOp>>>();

  /**
   * @param compOp The {@link CompOp} to use. Should not be <code>null</code>.
   * @param valueClass The class to be check. Should not be <code>null</code>.
   * @return <code>true</code> if the values of the given class may be used for the given {@link CompOp}.
   */
  @SuppressWarnings("unchecked")
  public boolean isCompOpApplicableForValueClass(Class<? extends CompOp> compOp, Class<?> valueClass) {
    // check the fast access map
    Set<Class<CompOp>> ops = getCompOpsForValueClass(valueClass);
    if (ops.contains(compOp)) {
      return true;
    }

    // check all compops for compatibility and enhance the fast access map.
    // XXX oboede: if required, a fast fail map may be added to prevent repeated scans here.
    for (Class<CompOp> coc : compOpToValueClassesMap.keySet()) {
      if (coc.isAssignableFrom(compOp)) {
        Set<Class<?>> valueClasses = getValueClassesForCompOp(compOp);
        for (Class<?> vc : valueClasses) {
          if (vc.isAssignableFrom(valueClass)) {
            ops.add((Class<CompOp>)compOp);
            return true;
          }
        }
      }
    }

    return false;
  }

  public List<Class<CompOp>> findCompsForValueClass(Class<?> valueClass) {
    List<Class<CompOp>> compOps = new ArrayList<Class<CompOp>>();
    for (Map.Entry<Class<CompOp>, Set<Class<?>>> e : compOpToValueClassesMap.entrySet()) {
      for (Class<?> c : e.getValue()) {
        if (c.isAssignableFrom(valueClass)) {
          compOps.add(e.getKey());
          break;
        }
      }
    }
    return compOps;
  }

  /**
   * Registers a matching {@link CompOp} value class pair.
   *
   * @param compOp The {@link CompOp}. Should not be <code>null</code>.
   * @param valueClass The value class. Should not be <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public synchronized void registerCompOp(Class<? extends CompOp> compOp, Class<?> valueClass) {
    Set<Class<?>> valueClasses = getValueClassesForCompOp((Class<CompOp>)compOp);
    valueClasses.add(valueClass);

    Set<Class<CompOp>> compOps = getCompOpsForValueClass(valueClass);
    compOps.add((Class<CompOp>)compOp);
  }

  private Set<Class<CompOp>> getCompOpsForValueClass(Class<?> valueClass) {
    Set<Class<CompOp>> ops = valueClassToCompOpsMap.get(valueClass);
    if (ops == null) {
      ops = new HashSet<Class<CompOp>>();
      valueClassToCompOpsMap.put(valueClass, ops);
    }
    return ops;
  }

  @SuppressWarnings("unchecked")
  private Set<Class<?>> getValueClassesForCompOp(Class<? extends CompOp> compOp) {
    Set<Class<?>> valueClasses = compOpToValueClassesMap.get(compOp);
    if (valueClasses == null) {
      valueClasses = new HashSet<Class<?>>();
      compOpToValueClassesMap.put((Class<CompOp>)compOp, valueClasses);
    }
    return valueClasses;
  }


}
