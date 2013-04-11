package org.pm4j.common.query;

import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation for a filter compare definition factory.
 * <p>
 * It uses a map of value types to related {@link CompOp}s and a default
 * set of {@link CompOp}s.
 *
 * @author OBOEDE
 */
public class FilterCompareDefinitionFactoryImpl implements FilterCompareDefinitionFactory {

  private Map<Class<?>, CompOp[]> valueTypeToCompOpsMap = new HashMap<Class<?>, CompOp[]>();
  private CompOp[] defaultCompOps = null;


  /**
   * Defines the compare operations offered for an attribute value type.
   *
   * @param valueType the attribute value type to define the compare operators for.
   * @param compOps the applicable set of compare operators.
   */
  public void defineCompOpsForValueType(Class<?> valueType, CompOp... compOps) {
    assert valueType != null;
    valueTypeToCompOpsMap.put(valueType, compOps);
  }

  /**
   * Defines the compare operator used if no explicitely defined type mapping did match.
   *
   * @param compOps the default compare operations.
   */
  public void setDefaultCompOps(CompOp... compOps) {
    defaultCompOps = compOps;
  }

  /**
   * Creates an attribute value type specific filter definition.
   *
   * @param attr
   *            the attribute to create a filter definition for.
   * @return the filter definition.
   */
  @Override
  public FilterCompareDefinition createCompareDefinition(QueryAttr attr) {
	  CompOp[] comps = getCompOpsForValueType(attr);

	  if (comps == null) {
	      throw new RuntimeException("No compops defined for filter value type: " + attr.getType());
	  }

      FilterCompareDefinition fcd = new FilterCompareDefinition(attr, comps);
      return fcd;
  }

  protected CompOp[] getCompOpsForValueType(QueryAttr attr) {
	Class<?> valueType = attr.getType();

    // first try exact type matches
    CompOp[] ops = valueTypeToCompOpsMap.get(valueType);

    if (ops != null) {
      return ops;
    }

    // then try compatible type matches (e.g. for enum types)
    // XXX olaf: may be optimized by matching only non-final types.
    for (Map.Entry<Class<?>, CompOp[]> e : valueTypeToCompOpsMap.entrySet()) {
      if (e.getKey().isAssignableFrom(valueType)) {
        return e.getValue();
      }
    }

    // no explicite type match:
    if (defaultCompOps == null) {
      throw new RuntimeException("Unsuppported filter value type: " + valueType);
    }

    return defaultCompOps;
  }

}
