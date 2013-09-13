package org.pm4j.core.pm.impl.cache;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;

/**
 * A helper that may be used to handle a shared named object instance within a
 * {@link PmConversation}.<br>
 * It may be used by several objects to use only a single shared named object
 * instance.<br>
 * This is useful to get a shared caching service for multiple PM instances
 * (e.g. table rows PM's).
 * <p>
 * This class provides a named object identifier and a factory method that is used to
 * create the requested named object on the fly if it does not yet exist.
 *
 * @param <T>
 *          type of the handled named object.
 *
 * @author olaf boede
 */
public abstract class SharedNamedObjectRef<T> {

  /** A common name prefix used for all shared objects handled by this class. */
  public static final String SHARED_OBJ_PREFIX = "_sharedObj_";

  private final PmObject pmCtxt;
  private final String name;
  private final Class<?> objectType;
  private T namedObj;

  /**
   * Creates a shared referece for an instance that is identified by
   * <code>'_sharedObj_' + objectType.getName()</code>.
   *
   * @param pmCtxt
   *          the PM used to get the conversation with the named object
   *          reference.
   * @param objectType
   *          the object type used for identification.
   */
  public SharedNamedObjectRef(PmObject pmCtxt, Class<?> objectType) {
    assert pmCtxt != null;
    assert objectType != null;

    this.pmCtxt = pmCtxt;
    this.objectType = objectType;
    this.name = SHARED_OBJ_PREFIX + objectType.getName();
  }

  /**
   * Creates a shared referece for an instance that is identified by
   * <code>'_sharedObj_' + sharedObjName</code>.
   *
   * @param pmCtxt
   *          the PM used to get the conversation with the named object
   *          reference.
   * @param sharedObjName
   *          the name that identifies the shared object.
   */
  public SharedNamedObjectRef(PmObject pmCtxt, String sharedObjName) {
    assert pmCtxt != null;
    assert StringUtils.isNotBlank(sharedObjName);

    this.pmCtxt = pmCtxt;
    this.objectType = null;
    this.name = SHARED_OBJ_PREFIX + sharedObjName;
  }

  /**
   * Gets the shared instance from the related {@link PmConversation}.
   * <p>
   * If instance was found for the requuested object name, an named object will
   * be created using the method {@link #create()}.
   *
   * @return the shared named object.
   */
  @SuppressWarnings("unchecked")
  public T getRef() {
    if (namedObj == null) {
      namedObj = (T) pmCtxt.getPmConversation().getPmNamedObject(name);
      if (namedObj == null) {
        namedObj = create();
        pmCtxt.getPmConversation().setPmNamedObject(name, namedObj);
      }
    }
    return namedObj;
  }

  /**
   * A factory method used to create the shared named object if it was not found
   * within the related {@link PmConversation}.
   *
   * @return the named object to register.
   */
  protected abstract T create();

  /**
   * Provides the PM context object used to get the {@link PmConversation} that
   * handles the named objects.
   *
   * @return the PM provided as constructor parameter.
   */
  public PmObject getPmCtxt() {
    return pmCtxt;
  }

  /**
   * Provides the name of the named object to provide.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Provides the optional object type that was provided if the constructor with
   * object type parameter was used.
   *
   * @return the object type or <code>null</code>.
   */
  public Class<?> getObjectType() {
    return objectType;
  }

}
