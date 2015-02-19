package org.pm4j.core.pm.impl.title;

import java.util.List;
import java.util.Locale;

/**
 * An interface for a UI resource string provider.
 *
 * @author OBOEDE
 */
public interface ResourceStringProvider {

  String findResourceString(Locale locale, List<Class<?>> resLoadCtxtClasses, String key);

}
