package org.pm4j.core.pm.impl.inject;

import java.util.ArrayList;
import java.util.List;

import org.pm4j.core.pm.PmDefaults;

public class DiResolverUtil  {

  private static final DiResolver[] EMPTY_ARRAY = new DiResolver[] {};

  public static DiResolver[] getDiResolvers(Class<?> forClass) {
    List<DiResolver> resolverList = new ArrayList<DiResolver>();
    for (DiResolverFactory f : PmDefaults.getInstance().getDiResolverFactories()) {
      DiResolver r = f.makeDiResolver(forClass);
      if (r != null) {
        resolverList.add(r);
      }
    }
    return resolverList.isEmpty()
        ? EMPTY_ARRAY
        : resolverList.toArray(new DiResolver[resolverList.size()]);
  }

}
