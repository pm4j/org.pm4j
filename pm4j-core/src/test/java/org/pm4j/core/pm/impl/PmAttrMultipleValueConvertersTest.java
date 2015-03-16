package org.pm4j.core.pm.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.converter.value.ValueConverterChain;
import org.pm4j.common.converter.value.ValueConverter;
import org.pm4j.common.converter.value.ValueConverterCtxt;
import org.pm4j.common.converter.value.ValueConverterDefault;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

/**
 * Test for multiple {@link ValueConverter} feature.
 * 
 * @author alech
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PmAttrMultipleValueConvertersTest {

  static abstract class BaseTestConverter implements ValueConverter<Integer, Integer> {
    boolean toExternalUsed;
    boolean toInternalUsed;
  }

  /**
   * A test converter that stores values increased by 2
   */
  public static final class IncreaseBy2Converter extends BaseTestConverter {

    @Override
    public Integer toExternalValue(ValueConverterCtxt ctxt, Integer i) {
      toExternalUsed = true;
      return i += 2;
    }

    @Override
    public Integer toInternalValue(ValueConverterCtxt ctxt, Integer e) {
      toInternalUsed = true;
      return e -= 2;
    }

  }

  /**
   * 
   * A test converter that stores values multiplied by 2
   */
  public static final class MultipleBy2Converter extends BaseTestConverter {

    @Override
    public Integer toExternalValue(ValueConverterCtxt ctxt, Integer i) {
      toExternalUsed = true;
      return i * 2;
    }

    @Override
    public Integer toInternalValue(ValueConverterCtxt ctxt, Integer e) {
      toInternalUsed = true;
      return e / 2;
    }
  }

  public static final class TestPm extends PmConversationImpl {

    @PmAttrCfg
    public final PmAttrIntegerImpl simpleAttr = new PmAttrIntegerImpl(this);

    @PmAttrCfg(valueConverter = IncreaseBy2Converter.class)
    public final PmAttrIntegerImpl singleConverterAttr = new PmAttrIntegerImpl(this);

    @PmAttrCfg(valueConverter = { IncreaseBy2Converter.class, MultipleBy2Converter.class })
    public final PmAttrIntegerImpl multipleConvertedAttr = new PmAttrIntegerImpl(this);

  }

  private TestPm testPm;

  @Before
  public void setupPm() {
    testPm = new TestPm();
  }

  /**
   * Tests default conversion
   */
  @Test
  public void testDefaultConversionAttr() {
    testConverters(testPm.simpleAttr);
  }

  /**
   * Tests single provided {@link ValueConverter}
   */
  @Test
  public void testSingleConverterAttr() {
    testConverters(testPm.singleConverterAttr, IncreaseBy2Converter.class);
  }

  /**
   * Tests multiple provided {@link ValueConverter}
   */
  @Test
  public void testMultipleConvertersAttr() {
    testConverters(testPm.multipleConvertedAttr, IncreaseBy2Converter.class, MultipleBy2Converter.class);
  }

  private void testConverters(PmAttrIntegerImpl pm, Class<? extends BaseTestConverter>... convertersExpected) {

    // first test for setValueAsString and getValue equality
    pm.setValueAsString("10");
    Assert.assertEquals((Integer) 10, pm.getValue());

    ValueConverter<Integer, Integer> pmValueConverter = pm.getValueConverter();

    // test proper internal implementation
    
    if (convertersExpected.length == 0) {
      Assert.assertEquals(pmValueConverter, ValueConverterDefault.INSTANCE);
    } else if (convertersExpected.length == 1) {
      //no Chained converter should be used!
      Assert.assertTrue("Expected converter instance of " + convertersExpected[0], pmValueConverter.getClass().equals(convertersExpected[0]));
    } else { // more than one

      Assert.assertTrue("Expected converter instance of ChainedValueConverter", pmValueConverter instanceof ValueConverterChain);
      List<ValueConverter> chainedConverters = ((ValueConverterChain) pmValueConverter).getValueConverters();

      for (Class<? extends BaseTestConverter> expectedConverter : convertersExpected) {
        // locate expected converter
        BaseTestConverter expectedConverterInstance = null;
        for (ValueConverter chainedConverterInstance : chainedConverters) {
          if (chainedConverterInstance.getClass().equals(expectedConverter)) {
            expectedConverterInstance = (BaseTestConverter) chainedConverterInstance;
            break;
          }
        }
        Assert.assertNotNull("Expected converter to be used: " + expectedConverter, expectedConverterInstance);
        Assert.assertTrue("Expecteed toInternal call in converter: " + expectedConverter, expectedConverterInstance.toInternalUsed);
        Assert.assertTrue("Expecteed toExternalUsed call in converter: " + expectedConverter, expectedConverterInstance.toExternalUsed);
      }
    }
  }

}
