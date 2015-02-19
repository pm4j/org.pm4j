package org.pm4j.core.util.table;

import org.apache.commons.lang.StringUtils;

/**
 * Specification of a column size.
 * <p>
 * XXX: In the first implementation step, only the <code>preferredSize</code> is
 * supported. The column size specification may be enhanced with min- and
 * max-Sizes in the future.
 */
public class ColSizeSpec {

  /** Size specification unit. */
  public static enum Unit {
    /** A relative size. */
    REL,
    /** A fix size. Vies should layout this as a pixel-size. */
    ABS,
    /** Marks this size specification as not specified. */
    UNSPECIFIED
  };

  /**
   * A size specification. May be expressed using different <code>unit</code>s.
   */
  public static class SizeSpec {

    public static final String ABS_UNIT_STRING = "pt";

    private final int          value;
    private final Unit         unit;

    /**
     * Defines a unit with a string. There are three supported variants:
     * <ul>
     * <li>'123pt': specifies an absolute size of '123' Points.</li>
     * <li>'123': specifies a relative size.</li>
     * <li>'': specifies an undefined size.</li>
     * </ul>
     * 
     * @param s
     *          The size speciification.
     */
    public SizeSpec(String s) {
      if (StringUtils.isBlank(s)) {
        this.value = 0;
        this.unit = Unit.UNSPECIFIED;
      } else {
        String valString = s.trim();
        if (valString.endsWith(ABS_UNIT_STRING)) {
          valString = valString.substring(0, valString.length() - ABS_UNIT_STRING.length()).trim();
          this.unit = Unit.ABS;
        } else {
          this.unit = Unit.REL;
        }
        this.value = Integer.parseInt(valString);
      }
    }

    public SizeSpec(int value, Unit unit) {
      this.value = value;
      this.unit = unit;
    }

    public int getValue() {
      return value;
    }

    public Unit getUnit() {
      return unit;
    }

    @Override
    public String toString() {
      return "" + value + " " + unit;
    }
  }

  private SizeSpec prefSize;
  private SizeSpec minSize;
  private SizeSpec maxSize;

  public ColSizeSpec(String prefSize, String minSize, String maxSize) {
    this.prefSize = new SizeSpec(prefSize);
    this.minSize = new SizeSpec(minSize);
    this.maxSize = new SizeSpec(maxSize);
  }

  /**
   * Creates a column size specification with preferred size data.
   * 
   * @param value
   * @param unit
   */
  public ColSizeSpec(int value, Unit unit) {
    this(new SizeSpec(value, unit), null, null);
  }

  public ColSizeSpec(SizeSpec preferredSize, SizeSpec minSize, SizeSpec maxSize) {
    this.prefSize = preferredSize;
    this.minSize = minSize;
    this.maxSize = maxSize;
  }

  public SizeSpec getPrefSize() {
    return prefSize;
  }

  public SizeSpec getMinSize() {
    return minSize;
  }

  public SizeSpec getMaxSize() {
    return maxSize;
  }

  @Override
  public String toString() {
    return getPrefSize().toString();
  }
}
