package org.pm4j.util.resource;

import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.pm4j.common.util.resource.ClassPathResourceFinder;

/**
 * Resource finder test cases.
 */
public class ClassPathResourceFinderTest extends TestCase {

  /**
   * Saves the locale before the test and restores it after the test so the test is able to play around with it.
   */
  private Locale defaultLocale;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    defaultLocale = Locale.getDefault();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    Locale.setDefault(defaultLocale);
  }

  /**
   * Just checks how the resource bundle strategy without default language
   * fallback works.
   */
  public void testResourceBundleFallback() {
    Locale.setDefault(Locale.CHINESE);

    ResourceBundle b = ResourceBundle.getBundle("Resources", Locale.ENGLISH);
    assertEquals("d/M/yy|dd/MM/yyyy", b.getString("pmAttrDate_defaultFormat"));
    b = ResourceBundle.getBundle("Resources", Locale.GERMAN);
    assertEquals("d.M.yy|dd.MM.yyyy", b.getString("pmAttrDate_defaultFormat"));

    Locale.setDefault(Locale.GERMAN);

    // XXX: The default fallback did not work reliable on all systems.
    //      Fortunately we did not have that problem with an explicitly
    //      defined strategy, as shown later in this test...
    //
    // b = ResourceBundle.getBundle("Resources", Locale.ENGLISH);
    // uses the german file as fallback:
    // assertEquals("dd.MM.yyyy", b.getString("pmAttrDate_defaultFormat"));

    // no ..._en file. default resource file used.
    b = ResourceBundle.getBundle("Resources", Locale.ENGLISH, ClassPathResourceFinder.instance().getResBundleStrategy());
    assertEquals("d/M/yy|dd/MM/yyyy", b.getString("pmAttrDate_defaultFormat"));

    b = ResourceBundle.getBundle("Resources", Locale.GERMAN, ClassPathResourceFinder.instance().getResBundleStrategy());
    assertEquals("d.M.yy|dd.MM.yyyy", b.getString("pmAttrDate_defaultFormat"));
  }

  /**
   * Check the language fallback using the black box perspective.
   */
  public void testResFinderFallback() {
    ClassPathResourceFinder finder = ClassPathResourceFinder.instance();

    Locale.setDefault(Locale.CHINESE);

    assertEquals("d/M/yy|dd/MM/yyyy", finder.findString(getClass(), "pmAttrDate_defaultFormat", Locale.ENGLISH));
    assertEquals("d.M.yy|dd.MM.yyyy", finder.findString(getClass(), "pmAttrDate_defaultFormat", Locale.GERMAN));

    Locale.setDefault(Locale.GERMAN);

    assertEquals("d/M/yy|dd/MM/yyyy", finder.findString(getClass(), "pmAttrDate_defaultFormat", Locale.ENGLISH));
    assertEquals("d.M.yy|dd.MM.yyyy", finder.findString(getClass(), "pmAttrDate_defaultFormat", Locale.GERMAN));
  }

}
