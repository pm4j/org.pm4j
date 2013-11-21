package org.pm4j.core.pm.impl.converter;

import java.io.Serializable;

import org.pm4j.common.converter.string.StringConverterBase;
import org.pm4j.common.converter.string.StringConverterCtxt;

/**
 * @deprecated please use {@link StringConverterBase}.
 */
@Deprecated
public abstract class PmConverterSerializeableBase<T extends Serializable> extends StringConverterBase<T, StringConverterCtxt> {

}
