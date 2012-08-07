package org.pm4j.core.pm.api;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.impl.PmConversationImpl;

public class PmLocalizeApiTest {

  private PmConversation pmCtxt = new PmConversationImpl() {};

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

}
