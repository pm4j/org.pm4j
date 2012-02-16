package org.pm4j.core.pm.impl;

import java.util.Locale;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.converter.PmConverterBoolean;
import org.pm4j.core.pm.impl.options.PmOptionImpl;
import org.pm4j.core.pm.impl.options.PmOptionSetImpl;

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
      ? titleForNameAndValueKey(value, getPmConversation().getPmLocale())
      : null;
  }

  @Override
  protected PmOptionSet getOptionSetImpl() {
    PmOptionSetImpl pmOptionSetBean = new PmOptionSetImpl();

    Locale locale = getPmConversation().getPmLocale();
    String title = titleForNameAndValueKey(Boolean.TRUE, locale);
    pmOptionSetBean.addOption(new PmOptionImpl(Boolean.TRUE.toString(), title, Boolean.TRUE));
    title = titleForNameAndValueKey(Boolean.FALSE, locale);
    pmOptionSetBean.addOption(new PmOptionImpl(Boolean.FALSE.toString(), title, Boolean.FALSE));

    return pmOptionSetBean;
  }

  // -- Helper --

  private String titleForNameAndValueKey(Object value, Locale locale) {
    return PmLocalizeApi.localize(this, ResKeyUtil.classNameAndValue(value), locale);
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
