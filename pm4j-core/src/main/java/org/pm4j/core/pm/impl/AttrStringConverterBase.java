package org.pm4j.core.pm.impl;

import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterBase;
import org.pm4j.core.pm.PmAttr;

/**
 * A convenience converter base class for {@link StringConverter}s that operate for {@link PmAttr}
 * classes.
 *
 * @param <T>
 *          Type of the value to convert.
 *
 * @author Olaf Boede
 */
public abstract class AttrStringConverterBase<T> extends StringConverterBase<T, AttrConverterCtxt> {
}
