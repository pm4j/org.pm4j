package org.pm4j.core.pm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.pm4j.common.converter.string.StringConverter;
import org.pm4j.common.converter.string.StringConverterInteger;
import org.pm4j.common.converter.string.StringConverterList;
import org.pm4j.common.converter.string.StringConverterLong;
import org.pm4j.common.converter.string.StringConverterParseException;
import org.pm4j.common.converter.string.StringConverterString;
import org.pm4j.common.converter.string.StringConverterToString;
import org.pm4j.common.util.reflection.ClassUtil;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.pm.PmAttrList;
import org.pm4j.core.pm.PmAttrNumber;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrListCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;

/**
 * Provides an implementation for PMs that can bind to {@link Collection} values.
 *
 * @author olaf boede
 *
 * @param <T> List item type
 */
public class PmAttrListImpl<T> extends PmAttrBase<List<T>, List<T>> implements PmAttrList<T> {

  /** Binds to a {@link Collection} of {@link Long}s. */
  @PmAttrListCfg(itemStringConverter=StringConverterLong.class)
  public static class Longs extends PmAttrListImpl<Long> {
    public Longs(PmObject pmParent) { super(pmParent); }
    @Override
    protected String getFormatDefaultResKey() {
      return PmAttrNumber.RESKEY_DEFAULT_INTEGER_FORMAT_PATTERN;
    }
  }

  /** Binds to a {@link Collection} of {@link Integer}s. */
  @PmAttrListCfg(itemStringConverter=StringConverterInteger.class)
  public static class Integers extends PmAttrListImpl<Integer> {
    public Integers(PmObject pmParent) { super(pmParent); }
    @Override
    protected String getFormatDefaultResKey() {
      return PmAttrNumber.RESKEY_DEFAULT_INTEGER_FORMAT_PATTERN;
    }
  }

  /** Binds to a {@link Collection} of {@link String}s. */
  @PmAttrListCfg(itemStringConverter=StringConverterString.class)
  public static class Strings extends PmAttrListImpl<String> {
    public Strings(PmObject pmParent) { super(pmParent); }
  }

  public PmAttrListImpl(PmObject pmParent) {
    super(pmParent);
  }

  public List<T> getValueAsList() {
    return getValue();
  }

  public void setValueAsList(List<T> value) {
    setValue(value);
  }

  @Override
  public List<String> getValueAsStringList() {
    List<T> items = getValue();
    if (items != null) {
      StringConverter<T> c = getItemStringConverterImpl();
      List<String> stringList = new ArrayList<String>(items.size());
      for (T item : items) {
        stringList.add(c.valueToString(getConverterCtxt(), item));
      }
      return stringList;
    }
    else {
      return Collections.emptyList();
    }
  }

  @Override @SuppressWarnings("unchecked")
  public void setValueAsStringList(List<String> value) throws PmConverterException {
    if (value != null && !value.isEmpty()) {
      StringConverter<T> c = getItemStringConverterImpl();
      List<T> items = new ArrayList<T>(value.size());
      for (String s : value) {
        try {
          items.add(c.stringToValue(getConverterCtxt(), s));
        } catch (StringConverterParseException e) {
          throw new PmConverterException(this, e);
        }
      }
      setValue(items);
    }
    else {
      setValue(Collections.EMPTY_LIST);
    }
  }

  /**
   * Get the bean field type converted to the external PM api type.
   * In case the list is empty this same list shall be populated with default value(s).
   * This is different from the super implementation which returns a new list instance with default value(s).
   */
  // TODO oboede: change consequently to value converter implementation!
  @Override
  protected List<T> getValueImpl() {
    List<T> beanAttrValue = getBackingValue();
    if(beanAttrValue!=null) {
      List<T> pmValue = getValueConverter().toExternalValue(getConverterCtxt(), beanAttrValue);
      if(pmValue.isEmpty() && !isValueChangedBySetValue()) {
        // in case the list is empty the elements from the default list are copied over
        pmValue.addAll(getDefaultValue());
        return pmValue;
      }
    }
    return super.getValueImpl();
  }

  /**
   * The item-{@link StringConverter} can be configured using the annotation
   * {@link PmAttrListCfg#itemStringConverter()} or by overriding this method.
   *
   * @return The {@link StringConverter} used for the list item values.
   */
  @SuppressWarnings("unchecked")
  protected StringConverter<T> getItemStringConverterImpl() {
    return (StringConverter<T>) ((MetaData)getPmMetaDataWithoutPmInitCall()).itemStringConverter;
  }

  @Override
  protected boolean isEmptyValue(List<T> value) {
    return (value == null) || value.isEmpty();
  }

  @Override
  public List<T> getValueSubset(int fromIdx, int numItems) {
    List<T> all = getValue();
    int toRow = (numItems == -1)
        ? all.size()
        : Math.min(fromIdx+numItems, all.size());

    List<T> subList = all.subList(fromIdx, toRow);
    return subList;
  }

  @Override
  public int getSize() {
    List<T> value = getValue();
    return (value != null) ? value.size() : 0;
  }

  @Override
  public NullOption getNullOptionDefault() {
    return NullOption.NO;
  }

  /**
   * If there is no expression based default value defined, an empty list will be generated
   * as default value.
   */
  @Override
  protected List<T> getDefaultValueImpl() {
    List<T> value = super.getDefaultValueImpl();
    if (value == null) {
      value = new ArrayList<T>();
    }
    return value;
  }

  /** Lazy initialization of string converter. */
  @Override
  protected StringConverter<List<T>> getStringConverterImpl() {
    MetaData md = getOwnMetaData();
    // the java implementation may overrule the configured value.
    md.itemStringConverter = getItemStringConverterImpl();

    @SuppressWarnings("unchecked")
    StringConverterList<T> stringConverter = new StringConverterList<T>((StringConverter<T>)md.itemStringConverter);
    stringConverter.setStringSeparator(md.itemStringSeparator);

    return stringConverter;
  }

  // ======== meta data ======== //

  @Override
  protected PmObjectBase.MetaData makeMetaData() {
    return new MetaData();
  }

  protected void initMetaData(PmObjectBase.MetaData metaData) {
    super.initMetaData(metaData);
    MetaData myMetaData = (MetaData) metaData;
    
    PmAttrListCfg annotation = AnnotationUtil.findAnnotation(this, PmAttrListCfg.class);
    if (annotation != null) {
      if (annotation.itemStringConverter() != StringConverterToString.class) {
        myMetaData.itemStringConverter = ClassUtil.newInstance(annotation.itemStringConverter());
        myMetaData.itemStringSeparator = annotation.valueStringSeparator();
      }
    }
  }

  protected static class MetaData extends PmAttrBase.MetaData {
      private StringConverter<?> itemStringConverter = StringConverterToString.INSTANCE;
      private String itemStringSeparator = PmAttrListCfg.DEFAULT_STRING_ITEM_SEPARATOR;

    public MetaData() {
      super(Integer.MAX_VALUE); // maximum valueAsString characters.
    }
}

  private final MetaData getOwnMetaData() {
    return (MetaData) getPmMetaData();
  }

}

