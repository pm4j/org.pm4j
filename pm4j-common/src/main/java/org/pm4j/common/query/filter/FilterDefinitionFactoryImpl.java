package org.pm4j.common.query.filter;

import java.util.HashMap;
import java.util.Map;

import org.pm4j.common.query.CompOp;
import org.pm4j.common.query.CompOpContains;
import org.pm4j.common.query.CompOpEquals;
import org.pm4j.common.query.CompOpGt;
import org.pm4j.common.query.CompOpIsNull;
import org.pm4j.common.query.CompOpLt;
import org.pm4j.common.query.CompOpNotContains;
import org.pm4j.common.query.CompOpNotEquals;
import org.pm4j.common.query.CompOpStartsWith;
import org.pm4j.common.query.QueryAttr;
import org.pm4j.common.query.QueryOptions;

/**
 * A default implementation for a filter compare definition factory.
 * <p>
 * It uses a map of value types to related {@link CompOp}s and a default
 * set of {@link CompOp}s.
 *
 * @author OBOEDE
 */
public class FilterDefinitionFactoryImpl implements FilterDefinitionFactory {

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
  public FilterDefinition createCompareDefinition(QueryAttr attr) {
	  CompOp[] comps = getCompOpsForValueType(attr);

	  if (comps == null) {
	      throw new RuntimeException("No compops defined for filter value type: " + attr.getType());
	  }

      FilterDefinition fcd = new FilterDefinition(attr, comps);
      return fcd;
  }

  /**
   * Creates a {@link FilterDefinition} for the given attribute and
   * adds it to the given query options.
   *
   * @param queryOptions the options to add the compare definition to.
   * @param attr the attribute to generate the compare definition for.
   * @return the generated compare definition.
   */
  public FilterDefinition addFilter(QueryOptions queryOptions, QueryAttr attr) {
    FilterDefinition d = createCompareDefinition(attr);
    queryOptions.addFilterCompareDefinition(d);
    return d;
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

  /**
   * A default factory that just handles {@link String} and {@link Comparable} values.
   */
  public static class DefaultFactory extends FilterDefinitionFactoryImpl {

    /** The set of string compare operators. */
    public static final CompOp[] STRING_COMPOPS = {
            new CompOpStartsWith(),
            new CompOpEquals(),
            new CompOpNotEquals(),
            new CompOpContains(),
            new CompOpNotContains(),
            new CompOpIsNull() };

    /** The set of compare operators for other {@link Comparable} types. */
    public static final CompOp[] COMPAREABLE_COMPOPS = {
            new CompOpEquals(),
            new CompOpNotEquals(),
            new CompOpLt(),
            new CompOpGt(),
            new CompOpIsNull() };

    /**
     * Defines compare operators for strings and compareable.
     */
    public DefaultFactory() {
        defineCompOpsForValueType(String.class, STRING_COMPOPS);
        defineCompOpsForValueType(Comparable.class, COMPAREABLE_COMPOPS);
    }
  }
}
