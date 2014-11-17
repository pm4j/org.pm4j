package org.pm4j.tools.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmBean;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmTable;
import org.pm4j.core.pm.PmTableCol;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * A set of {@link PmTable} related junit test support methods.
 *
 * @author Olaf Boede
 */
public class PmTableAssert {

  private PmTableAssert() {
  }

  /**
   * Checks the set of localized column cell string values.
   *
   * @param expected
   *          A comma separated string containing the column strings of all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStrings(String expected, PmTableCol col) {
    assertEquals(expected, getColStrings(col));
  }

  /**
   * Checks the set of localized column cell string values.
   *
   * @param msg
   *          A message to show in case of a mismatch.
   * @param expected
   *          A comma separated string containing the column strings of all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStrings(String msg, String expected, PmTableCol col) {
    assertEquals(msg, expected, getColStrings(col));
  }

  /**
   * Checks the set of localized column cell string values shown on the current
   * table page.
   *
   * @param expected
   *          A comma separated string containing the column strings of all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStringsOnCurrentPage(String expected, PmTableCol col) {
    assertEquals(expected, getColStringsOnCurrentPage(col));
  }

  /**
   * Checks the set of localized column cell string values shown on the current
   * table page.
   *
   * @param msg
   *          A message to show in case of a mismatch.
   * @param expected
   *          A comma separated string containing the column strings of all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStringsOnCurrentPage(String msg, String expected, PmTableCol col) {
    assertEquals(msg, expected, getColStringsOnCurrentPage(col));
  }

  /**
   * Checks the set of localized column cell string values shown for the selected table rows.
   *
   * @param msg
   *          A message to show in case of a mismatch.
   * @param expected
   *          A comma separated string containing the column strings of selected all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStringsOfSelection(String msg, String expected, PmTableCol col) {
    assertEquals(msg, expected, getColStringsOfSelection(col));
  }

  /**
   * Checks the set of localized column cell string values shown for the selected table rows.
   *
   * @param expected
   *          A comma separated string containing the column strings of selected all
   *          table rows.
   * @param col
   *          The column to show the values for.
   */
  public static void assertColStringsOfSelection(String expected, PmTableCol col) {
    assertEquals(expected, getColStringsOfSelection(col));
  }

  /**
   *
   * @param col
   * @return
   */
  public static String getColStringsOnCurrentPage(PmTableCol col) {
    @SuppressWarnings({ "unchecked" })
    PmTable<? extends PmBean<?>> table = (PmTable<? extends PmBean<?>>) col.getPmParent();
    String columnName = col.getPmName();
    List<String> strings = new ArrayList<String>();

    for (PmBean<?> r : table.getRowPms()) {
      strings.add(getCellAttr(r, columnName).getValueLocalized());
    }
    return StringUtils.join(strings, ", ");
  }

  /**
   *
   * @param col
   * @return
   */
  public static String getColStrings(PmTableCol col) {
    @SuppressWarnings({ "unchecked" })
    PmTable<? extends PmBean<?>> table = (PmTable<? extends PmBean<?>>) col.getPmParent();
    String columnName = col.getPmName();
    List<String> strings = new ArrayList<String>();

    for (PmBean<?> r : table.getPmPageableCollection()) {
      strings.add(getCellAttr(r, columnName).getValueLocalized());
    }
    return StringUtils.join(strings, ", ");
  }

  /**
  *
  * @param col
  * @return
  */
 public static String getColStringsOfSelection(PmTableCol col) {
   @SuppressWarnings({ "unchecked" })
   PmTable<? extends PmBean<?>> table = (PmTable<? extends PmBean<?>>) col.getPmParent();
   String columnName = col.getPmName();
   List<String> strings = new ArrayList<String>();

   for (PmBean<?> r : table.getPmPageableCollection().getSelection()) {
     strings.add(getCellAttr(r, columnName).getValueLocalized());
   }
   return StringUtils.join(strings, ", ");
 }

  private static PmAttr<?> getCellAttr(PmBean<?> rowPm, String columnName) {
    PmObject cell = PmUtil.findChildPm(rowPm, columnName);
    if (!(cell instanceof PmAttr<?>)) {
      throw new PmRuntimeException("Row cell for column name '" + columnName + "' not found. Found instance: " + cell);
    }
    return (PmAttr<?>) cell;
  }

}
