package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.pm4j.core.pm.PmAttr;
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

  // ======== meta data ======== //

  @Override
  protected PmOptionSetDef<?> makeOptionSetDef(PmOptionCfg cfg, Method getOptionValuesMethod) {
    // TODO olaf: make a single OS definition for enums.
    return cfg == null
              ? new EnumOptionSetDef(enumClass, getOptionValuesMethod)
              : new EnumOptionSetDefWithOptionCfg(this, enumClass, cfg, getOptionValuesMethod);
  }

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    // the default max length.
    return new MetaData(30);
  }

  @Override
  protected void initMetaData(org.pm4j.core.pm.impl.PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;

    myMetaData.setConverter(new PmConverterEnum());
  }

  private MetaData getOwnMetaData() {
    return (MetaData) getPmMetaDataWithoutPmInitCall();
  }

  // ======== converter ======== //

  public static class PmConverterEnum  implements PmAttr.Converter<Enum<?>> {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Enum<?> stringToValue(PmAttr<?> pmAttr, String s) {
      return s.length() > 0
                  ? Enum.valueOf(((PmAttrEnumImpl)pmAttr).enumClass, s)
                  : null;
    }

    @Override
    public String valueToString(PmAttr<?> pmAttr, Enum<?> v) {
      return v.name();
    }
  }

}
