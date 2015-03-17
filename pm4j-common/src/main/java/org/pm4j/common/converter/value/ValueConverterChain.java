/**
 * 
 */
package org.pm4j.common.converter.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.pm4j.common.util.reflection.ClassUtil;

/**
 * This converter allows to join in multiple converters into a chain so that the value will always go through all the nested converters. <br/>
 *
 * For instance lets imagine the situation that we have a product of type {@code A} and we want it to be converted to {@code C}. Assuming 
 * we already have converters  {@code AB (A -> B)} and {@code BC (B -> C)} we can use {@link ValueConverterChain} to build a {@code A -> C} converter. <br/>
 * 
 * Such converter will pass the value through {@code value -> AB -> BC -> C} to obtain C or {@code C -> BC -> AB -> value} to obtain original external value. 
 * 
 * @author alech
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"}) //we must operate on raw types in this kind of converter
public final class ValueConverterChain implements ValueConverter {
  
  private List<ValueConverter> chain;
  
  /**
   * Creates a {@link ValueConverterChain} instance basing on the {@code converters} provided.
   * @param converters converters classes to be used in the chain
   * @return a new instance of {@link ValueConverterChain}
   */
  public ValueConverterChain(Class<? extends ValueConverter> ... converters) {
    Validate.notNull(converters, "converters cannot be null");
    
    this.chain = new ArrayList<ValueConverter>(converters.length);
    
    for (Class<? extends ValueConverter> converter : converters) {
      this.chain.add((ValueConverter) ClassUtil.newInstance(converter));
    }
  }

  @Override
  public final Object toExternalValue(ValueConverterCtxt ctxt, Object value) {
    Object intermediateResult = value;

    for (int i = chain.size() - 1; i >= 0; i--) {
      intermediateResult = chain.get(i).toExternalValue(ctxt, intermediateResult);
    }
    return intermediateResult;
  }

  @Override
  public final Object toInternalValue(ValueConverterCtxt ctxt, Object value) {
    Object intermediateResult = value;

    for (int i = 0; i < chain.size() ; i++) {
      intermediateResult = chain.get(i).toInternalValue(ctxt, intermediateResult);
    }
    return intermediateResult;
  }
  
  public final List<ValueConverter> getValueConverters() {
    return Collections.unmodifiableList(chain);
  }

}
