package org.pm4j.common.pageable.querybased;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.pm4j.common.pageable.querybased.QueryServiceSerializationSupport.SerializeableServiceProvider2;
import org.pm4j.common.selection.Selection;

/**
 * A selection base class that supports serializable selections.
 *
 * @author Olaf Boede
 */
public abstract class QuerySelectionBase<T_ITEM, T_ID> implements Selection<T_ITEM>, Serializable {
  private static final long serialVersionUID = 1L;

  /** The service provider may be <code>null</code> in case of non-serializeable selections. */
  private QueryServiceSerializationSupport.SerializeableServiceProvider2 serviceProvider;

  /** Reference to the service that provides the selected objects.
   * Is transient because most services are not serializable. */
  transient private QueryService<T_ITEM, T_ID> service;

  /**
   * Initalizes the service used to retrieve the selected objects.
   * If the service implements {@link QueryServiceSerializationSupport}, this selection may be serialized.
   *
   * @param service The service that provides the selected objects. May not be <code>null</code>.
   */
  public QuerySelectionBase(QueryService<T_ITEM, T_ID> service) {
    assert service != null;
    this.service = service;

    // find the (optionally embedded) serializeable service provider.
    QueryService<T_ITEM, T_ID> s = service;
    if (s instanceof QueryServiceSerializationSupport) {
      this.serviceProvider = ((QueryServiceSerializationSupport)s).getSerializeableServiceProvider();
    }
  }

  @Override
  public boolean isEmpty() {
    return getSize() == 0L;
  }

  /**
   * Provides the service reference.<br>
   * After a de-serialization this reference may be <code>null</code>.
   * In this case a {@link SerializeableServiceProvider2} my restore the service reference on the fly.
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  protected QueryService<T_ITEM, T_ID> getService() {
    if (service == null) {
      if (serviceProvider != null) {
        service = (QueryService<T_ITEM, T_ID>) serviceProvider.getQueryService();
      }

      if (service == null) {
        throw new RuntimeException("Your QueryService does not support serialization of selections.\n" +
            "Please add the service interface QueryServiceSerializationSupport to get serializeable selections.");
      }
    }
    return service;
  }

  /**
   * In addition to the standard serialization the follwing is implemented here:
   * <ul>
   * <li>Check the serialization preconditions.</li>
   * <li>Handle the option of having a serializable service</li>
   * </ul> 
   * 
   * @param oos
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject();
    if (serviceProvider == null) {
      if (service instanceof Serializable) {
        oos.writeObject(service);
      } else {
        throw new IOException("Unable to serialize a selection. The query service should either implement QueryServiceSerializationSupport or Serializeable.\n" +
            "\tFound query service: " + service);
      }
    } else {
      oos.writeObject(null);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream ois) throws IOException {
    try {
      ois.defaultReadObject();
      service = (QueryService<T_ITEM, T_ID>) ois.readObject();
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

}