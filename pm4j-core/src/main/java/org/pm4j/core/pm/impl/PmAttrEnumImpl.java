package org.pm4j.core.pm.impl;

import java.lang.reflect.Method;

import org.pm4j.common.converter.string.StringConverterBase;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.options.EnumOptionSetDef;
import org.pm4j.core.pm.impl.options.EnumOptionSetDefWithOptionCfg;
import org.pm4j.core.pm.impl.options.PmOptionSetDef;

/**
 * PM for attributes with enum values.
 *
 * @author olaf boede
 *
 * @param <T_ENUM> The enum value type.
 */
public class PmAttrEnumImpl<T_ENUM extends Enum<T_ENUM>> extends PmAttrBase<T_ENUM, T_ENUM> implements PmAttrEnum<T_ENUM> {

  private Class<T_ENUM> enumClass;

  public PmAttrEnumImpl(PmObject pmParent, Class<T_ENUM> enumClass) {
    super(pmParent);
    assert enumClass != null;

    this.enumClass = enumClass;
  }

  @Override
  public String getValueLocalized() {
    T_ENUM value = getValue();
    return (value != null)
              ? PmLocalizeApi.localizeEnumValue(this, value)
              : null;
  }

  /**
   * For enums we did not find a way to read the external value type generics parameter.
   * Because of that we simply provide the known type by overriding this method.
   */
  @Override
  public Class<?> getValueType() {
    return getEnumClass();
  }

  protected String getTitleForEnumValue(Enum<?> value) {
    return value != null
            ? PmLocalizeApi.localizeEnumValue(this, value)
            : getOwnMetaData().getOptionSetDef().getNullOptionTitle(this);
  }

  /**
   * @return The represented enum class.
   */
  protected Class<T_ENUM> getEnumClass() {
    return enumClass;
  }

  @Override
  protected PmOptionSetDef<?> makeOptionSetDef(PmOptionCfg cfg, Method getOptionValuesMethod) {
    // TODO olaf: make a single OS definition for enums.
    return cfg == null
              ? new EnumOptionSetDef(enumClass, getOptionValuesMethod)
              : new EnumOptionSetDefWithOptionCfg(this, enumClass, cfg, getOptionValuesMethod);
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    // the default max length.
    MetaData md = new MetaData(30);
//    md.setStringConverter(new PmConverterEnum());
    return md;
  }

  @Override
  protected void initMetaData(org.pm4j.core.pm.impl.PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    // TODO: the init call places a different string converter.
    // Because of that is was not possible to move the code to
    // makeMetaData(). --> Check!
    myMetaData.setStringConverter(new PmConverterEnum());
  }

  private MetaData getOwnMetaData() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  // ======== converter ======== //

  public static class PmConverterEnum extends StringConverterBase<Enum<?>, AttrConverterCtxt> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Enum<?> stringToValueImpl(AttrConverterCtxt ctxt, String s) throws Exception {
      return (s != null && !s.isEmpty())
                  ? Enum.valueOf(((PmAttrEnumImpl)ctxt.getPmAttr()).enumClass, s)
                  : null;
    }

    @Override
    protected String valueToStringImpl(AttrConverterCtxt ctxt, Enum<?> v) {
      return (v != null) ? v.name() : null;
    }

  }

}
