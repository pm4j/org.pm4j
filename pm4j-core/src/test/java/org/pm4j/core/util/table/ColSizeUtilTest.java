package org.pm4j.core.util.table;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.pm4j.core.util.table.ColSizeSpec.Unit;

public class ColSizeUtilTest extends TestCase {

	private List<ColSizeSpec> REL_SIZES = Arrays.asList(
			new ColSizeSpec(10, Unit.REL),
			new ColSizeSpec(20, Unit.REL),
			new ColSizeSpec(20, Unit.REL),
			new ColSizeSpec(50, Unit.REL)
	);
	
	public void testRelSizes() {
		assertEquals("[10, 20, 20, 50]",    Arrays.toString(ColSizeUtil.calcPreferredColSizes(REL_SIZES, 100)));
		assertEquals("[50, 100, 100, 250]", Arrays.toString(ColSizeUtil.calcPreferredColSizes(REL_SIZES, 500)));
	}

  private List<ColSizeSpec> ABS_SIZES = Arrays.asList(
      new ColSizeSpec(10, Unit.ABS),
      new ColSizeSpec(20, Unit.ABS),
      new ColSizeSpec(20, Unit.ABS),
      new ColSizeSpec(50, Unit.ABS)
  );
  
	public void testAbsSizes() {
    assertEquals("[10, 20, 20, 50]",    Arrays.toString(ColSizeUtil.calcPreferredColSizes(ABS_SIZES, 100)));
    assertEquals("[10, 20, 20, 50]",    Arrays.toString(ColSizeUtil.calcPreferredColSizes(ABS_SIZES, 500)));
	}
	
  private List<ColSizeSpec> MIXED_SIZES = Arrays.asList(
      new ColSizeSpec(10, Unit.REL),
      new ColSizeSpec(20, Unit.ABS),
      new ColSizeSpec(20, Unit.ABS),
      new ColSizeSpec(50, Unit.REL)
  );
  
  public void testMixedSizes() {
    assertEquals("[10, 20, 20, 50]",    Arrays.toString(ColSizeUtil.calcPreferredColSizes(MIXED_SIZES, 100)));
    assertEquals("[76, 20, 20, 384]",    Arrays.toString(ColSizeUtil.calcPreferredColSizes(MIXED_SIZES, 500)));
  }
  
}
