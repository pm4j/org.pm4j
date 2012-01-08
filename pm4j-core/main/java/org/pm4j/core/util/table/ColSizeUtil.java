package org.pm4j.core.util.table;

import java.util.Arrays;
import java.util.List;

import org.pm4j.core.util.table.ColSizeSpec.SizeSpec;
import org.pm4j.core.util.table.ColSizeSpec.Unit;

/**
 * Utility for column size calculations. 
 */
public final class ColSizeUtil {

  /**
   * Calculates the column widths for a given total width.
   * 
   * @param sizeSpecs
   *          The column size specifications.
   * @param totalWidth
   *          The total with available for display.
   * @return An array of width, containing the individual width of each column.<br>
   *         Is sorted in the same order as the providede <code>sizeSpecs</code>.
   */
  public static int[] calcPreferredColSizes(List<ColSizeSpec> sizeSpecs, int totalWidth) {
    int[] sizes = new int[sizeSpecs.size()];
    Arrays.fill(sizes, Integer.MIN_VALUE);

    int numOfUndefinedColSizes = 0;
    int numOfAbsColSizes = 0;
    
    // absolute sizes
    int absColsSizeSum = 0;
    for (int i = 0; i<sizes.length; ++i) {
      ColSizeSpec colSizeSpec = sizeSpecs.get(i);
      if (colSizeSpec != null) {
        SizeSpec colSize = colSizeSpec.getPrefSize();
        if (colSize.getUnit() == Unit.ABS) {
          absColsSizeSum += sizes[i] = colSize.getValue();
          ++numOfAbsColSizes;
        }
      }
      else {
        ++numOfUndefinedColSizes;
      }
    }
    
    // relative sizes
    int relColsTotalSpace = totalWidth - absColsSizeSum;
    int relUnitsSum = 0;
    for (int i = 0; i<sizes.length; ++i) {
      if (sizes[i] == Integer.MIN_VALUE) {
        ColSizeSpec spec = sizeSpecs.get(i);
        if (spec != null) {
          SizeSpec prefSize = spec.getPrefSize();
          relUnitsSum += prefSize.getValue();  
        }
      }
    }
    
    // TODO: Die nicht spezifizierten Spalten gleich verteilen...
    
    if (relUnitsSum > 0) {
      int sumOfAllRelCols = 0;
      for (int i = 0; i<sizes.length; ++i) {
        if (sizes[i] == Integer.MIN_VALUE) {
          ColSizeSpec spec = sizeSpecs.get(i);
          sumOfAllRelCols += sizes[i] = relColsTotalSpace * spec.getPrefSize().getValue() / relUnitsSum;
        }
      }
      // add remaining pixels to the last column:
      sizes[sizes.length-1] += relColsTotalSpace - sumOfAllRelCols; 
    }
    
    return sizes;
  }
  
}
