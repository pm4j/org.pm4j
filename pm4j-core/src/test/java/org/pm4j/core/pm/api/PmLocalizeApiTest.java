package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmLocalizeApiTest {

  public static class TestPm extends PmConversationImpl {
    /** The resource file defines the format: ###.##|##0.00 */
    public final PmAttrDouble aMultiFormatAttribute = new PmAttrDoubleImpl(this);
  }

  private TestPm pmCtxt = new TestPm();

  @Before
  public void setUp() {
    pmCtxt.setPmLocale(Locale.ENGLISH);
  }

  @Test
  public void testLocalizeSimpleText() {
    assertEquals("Get the English resource string.",
        "A simple text", PmLocalizeApi.localize(pmCtxt, "pmLocalizeApiTest.simpleText"));

    pmCtxt.setPmLocale(Locale.GERMAN);
    assertEquals("Get the German resource string",
    		"Ein einfacher Text", PmLocalizeApi.localize(pmCtxt, "pmLocalizeApiTest.simpleText"));

    assertEquals("A query to a non existing resource key generates a warning log and returns the key itself.",
        "--non-existing-resource-key--", PmLocalizeApi.localize(pmCtxt, "--non-existing-resource-key--"));
  }

  @Test
  public void testLocalizeWithParameterSet() {
    assertEquals("Resource string parameters are embedded in the localized string.",
        "Item 2 of 20", PmLocalizeApi.localize(pmCtxt, "pmLocalizeApiTest.localizeWithParameterSet", 2, 20));
  }

  @Test
  public void testFindLocalization() {
    assertEquals("Find an existing the English resource string.",
        "A simple text", PmLocalizeApi.findLocalization(pmCtxt, "pmLocalizeApiTest.simpleText"));

    assertEquals("A query for a non-existing resource string just provides 'null' (does not return a dummy as the localize() method).",
        null, PmLocalizeApi.findLocalization(pmCtxt, "--non-existing-resource-key--"));
  }

  @Test
  public void testLocalizationOfResourceThatIsDefinedOnlyForDefaultLanguage() {
    assertEquals("Get a resource that is defined only in English (default language) for English locale.",
        "Default language text", PmLocalizeApi.localize(pmCtxt, "pmLocalizeApiTest.simpleTextDefinedOnlyForDefaultLanguage"));

    pmCtxt.setPmLocale(Locale.GERMAN);
    assertEquals("Get a resource that is defined only in English (default language) for German locale. - Provides the English default text.",
        "Default language text", PmLocalizeApi.localize(pmCtxt, "pmLocalizeApiTest.simpleTextDefinedOnlyForDefaultLanguage"));
  }

  @Test
  public void testLocalizeOneOrMany() {
    assertEquals("If the provided number argument is 1, the resource key with the _one gets used.",
        "A single item", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.oneOrManyItems", 1));
    assertEquals("If the provided number argument is greter than one, the resource key with the _many gets used."
        + "The number parameter is the first resource string parameter in this case.",
        "3 items", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.oneOrManyItems", 3));
  }

  @Test
  public void testLocalizeNoneOneOrMany() {
    assertEquals("If the provided number argument is zero, the resource key with the _none gets used.",
        "No item found", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.noneOneOrManyItems", 0));
    assertEquals("If the provided number argument is 1, the resource key with the _one gets used.",
        "A single item", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.noneOneOrManyItems", 1));
    assertEquals("If the provided number argument is greter than one, the resource key with the _many gets used."
        + "The number parameter is the first resource string parameter in this case.",
        "3 items", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.noneOneOrManyItems", 3));
  }

  @Test
  public void testLocalizeItemsWithNoneFallback() {
    assertEquals("If the provided number argument is zero, the resource key with the _none gets used.",
        "There is no item.", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.itemsWithNoneFallback", 0));
    assertEquals("If the provided number argument is 1, the provided resource key will be used directly since there is no key with the _one postfix defined.",
        "1 items", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.itemsWithNoneFallback", 1));
    assertEquals("If the provided number argument is greter than one, the provided resource key will be used directly since there is no key with the _many postfix defined.",
        "3 items", PmLocalizeApi.localizeOneOrMany(pmCtxt, "pmLocalizeApiTest.itemsWithNoneFallback", 3));
  }

  @Test
  public void testOutputFormatString() {
    assertEquals("##0.00", PmLocalizeApi.getOutputFormatString(pmCtxt.aMultiFormatAttribute));
  }

}
