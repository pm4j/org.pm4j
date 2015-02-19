package org.pm4j.swt.util;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.pm4j.core.util.table.ColSizeSpec;
import org.pm4j.core.util.table.ColSizeUtil;

/**
 * A {@link ControlListener} that re-arranges the width of table columns
 * according to the size of the parent-{@link Composite} of the table.
 */
public class ColResizeListener extends ControlAdapter {

	private Composite comp;
	private Table table;
	private ColSizeSpec[] colSizes;
	
	public ColResizeListener(Table table, ColSizeSpec... colSizes) {
		this(table.getParent(), table, colSizes);
	}

	private ColResizeListener(Composite comp, Table table, ColSizeSpec... colSizes) {
		this.comp = comp;
		this.table = table;
		this.colSizes = colSizes;
		
		if (table.getColumnCount() != colSizes.length) {
			throw new IllegalArgumentException(
				"The number of table columns does not match the number of column size specifications.");
		}
	}
	
	public void controlResized(ControlEvent e) {
		Rectangle area = comp.getClientArea();
		Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int width = area.width - 2 * table.getBorderWidth();
		if (preferredSize.y > area.height + table.getHeaderHeight()) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}
		Point oldSize = table.getSize();
		if (oldSize.x > area.width) {
			// table is getting smaller so make the columns
			// smaller first and then resize the table to
			// match the client area width
			setColWidthForTotalWidth(width);
			table.setSize(area.width, area.height);
		} else {
			// table is getting bigger so make the table
			// bigger first and then make the columns wider
			// to match the client area width
			table.setSize(area.width, area.height);
			setColWidthForTotalWidth(width);
		}
	}
	  
	private void setColWidthForTotalWidth(int totalWidth) {
		TableColumn[] cols = table.getColumns();
		int[] widthArr = ColSizeUtil.calcPreferredColSizes(Arrays.asList(colSizes), totalWidth);
		for (int i=0; i<cols.length; ++i) {
			cols[i].setWidth(widthArr[i]);
		}
	}
}
