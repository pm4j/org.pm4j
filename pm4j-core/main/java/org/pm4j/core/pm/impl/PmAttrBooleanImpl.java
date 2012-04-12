package org.pm4j.core.pm.impl;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.converter.PmConverterBoolean;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetUtil;

public class PmAttrBooleanImpl extends PmAttrBase<Boolean, Boolean> implements PmAttrBoolean {

  public PmAttrBooleanImpl(PmObject pmParent) {
    super(pmParent);
  }

  /**
   * Provides a localized according to the current value.
   * <p>
   * Provides the localizations for the keys 'Boolean.TRUE' and 'Boolean.FALSE'.
   */
  @Override
  public String getValueLocalized() {
    Boolean value = getValue();
    return (value != null)
      ? PmLocalizeApi.localizeBooleanValue(this, value)
      : null;
  }

  @Override
  protected PmOptionSet getOptionSetImpl() {
    return new PmOptionSetImpl(PmOptionSetUtil.makeBooleanOptions(this));
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  @Override
  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);

    ((MetaData) metaData).setConverterDefault(PmConverterBoolean.INSTANCE);
  }

  protected static class MetaData extends PmAttrBase.MetaData {
    @Override
    protected int getMaxLenDefault() {
      // XXX olaf: check how to get a real language specific output without performance issues.
      return 10;
    }
  }

}
