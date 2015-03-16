/**
 * 
 */
package org.pm4j.common.converter.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.pm4j.common.util.reflection.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  private static final Logger LOG = LoggerFactory.getLogger(ValueConverterChain.class);
  
  private List<ValueConverter> chain;
  
  /**
   * Creates a {@link ValueConverterChain} instance basing on the {@code converters} provided.
   * @param converters converters classes to be used in the chain
   * @return a new instance of {@link ValueConverterChain}
   */
  public static ValueConverterChain of(Class<? extends ValueConverter> ... converters) {
    List<ValueConverter> converterInstances = new ArrayList<ValueConverter>(converters.length);
    for (Class<? extends ValueConverter> converter : converters) {
      converterInstances.add((ValueConverter) ClassUtil.newInstance(converter));
    }
    return new ValueConverterChain(converterInstances);
  }
  
  private ValueConverterChain(List<ValueConverter> chain) {
    if(chain == null) {
      throw new IllegalArgumentException("chain cannot be null");
    }
    this.chain = chain;
  }

  @Override
  public final Object toExternalValue(ValueConverterCtxt ctxt, Object i) {
    return apply(ctxt, i, true);
  }

  @Override
  public final Object toInternalValue(ValueConverterCtxt ctxt, Object e) {
    return apply(ctxt, e, false);
  }
  
  /**
   * Applies converter chain in defined order.
   * 
   * @param ctxt converter context
   * @param value given value to convert
   * @param inverse direction
   * @return a final product as a result of all the converters pass
   */
  private Object apply(ValueConverterCtxt ctxt, Object value, boolean inverse) {
    Object intermediateResult = value;

    for (ListIterator<ValueConverter> chainIterator = chain.listIterator(inverse ? chain.size() : 0); inverse ? chainIterator
        .hasPrevious() : chainIterator.hasNext();) {

      ValueConverter converter = null;
      try {
        if (inverse) {
          converter = chainIterator.previous();
          intermediateResult = converter.toExternalValue(ctxt, intermediateResult);
        } else {
          converter = chainIterator.next();
          intermediateResult = converter.toInternalValue(ctxt, intermediateResult);
        }
      } catch (RuntimeException e) {
        LOG.error("Error when applying converter {} to value {}", converter.getClass().getSimpleName(), intermediateResult, e);
        throw e;
      }
      LOG.trace("Applied value converter {} with result {}.", converter.getClass().getSimpleName(), intermediateResult);
    }
    return intermediateResult;
  }
  
  public final List<ValueConverter> getValueConverters() {
    return Collections.unmodifiableList(chain);
  }

}
