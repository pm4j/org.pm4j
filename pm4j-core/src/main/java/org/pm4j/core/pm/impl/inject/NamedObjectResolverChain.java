package org.pm4j.core.pm.impl.inject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link NamedObjectResolver} that iterates over a list of resolvers to
 * resolve a given name.
 * <p>
 * The iteration will be stopped when the first resolver finds a match.
 *
 * @author olaf boede
 *
 */
public class NamedObjectResolverChain implements NamedObjectResolver {

  private static final Log LOG = LogFactory.getLog(NamedObjectResolverChain.class);

  private List<NamedObjectResolver> resolvers = new ArrayList<NamedObjectResolver>();

  public NamedObjectResolverChain() {
  }

  public NamedObjectResolverChain(NamedObjectResolver... resolvers) {
    for (NamedObjectResolver r : resolvers) {
      if (!isNullResolver(r)) {
        this.resolvers.add(r);
      }
    }
  }

  public NamedObjectResolverChain(Iterable<NamedObjectResolver> resolvers) {
    for (NamedObjectResolver r : resolvers) {
      if (!isNullResolver(r)) {
        this.resolvers.add(r);
      }
    }
  }

  /**
   * Adds a resolver to the list of considered resolver items.
   *
   * @param r the resolver to add. Should not be <code>null</code>.
   */
  public void addResolver(NamedObjectResolver r) {
    assert r != null;
    resolvers.add(r);
  }

  @Override
  public Object findObject(String name) {
    Object o = null;
    for (NamedObjectResolver r : resolvers) {
      o = r.findObject(name);
      if (o != null) {
        if (LOG.isTraceEnabled()) {
          LOG.trace("Named object '" + name + "' was resolved to '" + o + "' by NamedObjectResolver '" + r + "'.");
        }
        break;
      }
    }

    return o;
  }


  public static NamedObjectResolver combineResolvers(NamedObjectResolver... itemsToCombine) {
    List<NamedObjectResolver> list = new ArrayList<NamedObjectResolver>();
    for (NamedObjectResolver r : itemsToCombine) {
      if (!isNullResolver(r)) {
        list.add(r);
      }
    }

    if (list.isEmpty()) {
      return NamedObjectResolverNullImpl.INSTANCE;
    }
    if (list.size() == 1) {
      return list.get(0);
    }
    return new NamedObjectResolverChain(list);
  }

  public static boolean isNullResolver(NamedObjectResolver r) {
    return (r == null) || (r instanceof NamedObjectResolverNullImpl);
  }

}
