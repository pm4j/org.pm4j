package org.pm4j.core.pm.impl.inject;

/**
 * This resolver is only intended as a place holder.<br>
 * It does never find an object.
 *
 * @author olaf boede
 */
public class NamedObjectResolverNullImpl implements NamedObjectResolver {

  /** A singleton like shared instance. */
  public static final NamedObjectResolver INSTANCE = new NamedObjectResolverNullImpl();

  @Override
  public Object findObject(String name) {
    return null;
  }

}
