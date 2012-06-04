package org.pm4j.core.pm;

/**
 * PM interface for attribute proxies.
 * <p>
 * An attribute proxy forwards its calls to a delegate behind it.
 *
 * @author olaf boede.
 *
 * @param <T> The attribute value type.
 */
public interface PmAttrProxy<T> extends PmAttr<T>{

  /**
   * @param delegate The delegate to forward the calls to.
   */
  void setDelegate(PmAttr<? extends T> delegate);

  /**
   * @return The delegate to forward the calls to.
   */
  PmAttr<?> getDelegate();

}
