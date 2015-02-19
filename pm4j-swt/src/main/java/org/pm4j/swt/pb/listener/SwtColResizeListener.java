package org.pm4j.swt.pb.listener;

/*
 * $Log: ColResizeListener.java,v $
 * Revision 1.1  2010-08-31 12:39:27  olaf
 * Story 15: Implementierung
 *
 */

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
public class SwtColResizeListener extends ControlAdapter {

	private Table table;
	private ColSizeSpec[] colSizes;

	/**
	 * @param table
	 *            The table to control the column sizes for.
	 * @param colSizes
	 *            The column size specification.<br>
	 *            The number of items should match the number of table columns.
	 */
	public SwtColResizeListener(Table table, ColSizeSpec... colSizes) {
		this.table = table;
		this.colSizes = colSizes;
		
		if (table.getColumnCount() != colSizes.length) {
			throw new IllegalArgumentException(
				"The number of table columns does not match the number of column size specifications.");
		}
	}
	
	public void controlResized(ControlEvent e) {
		Rectangle area = table.getParent().getClientArea();
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
			setColWidthsForTotalWidth(width);
			table.setSize(area.width, area.height);
		} else {
			// table is getting bigger so make the table
			// bigger first and then make the columns wider
			// to match the client area width
			table.setSize(area.width, area.height);
			setColWidthsForTotalWidth(width);
		}
	}
	  
	private void setColWidthsForTotalWidth(int totalWidth) {
		TableColumn[] cols = table.getColumns();
		int[] widthArr = ColSizeUtil.calcPreferredColSizes(Arrays.asList(colSizes), totalWidth);
		for (int i=0; i<cols.length; ++i) {
			cols[i].setWidth(widthArr[i]);
		}
	}
}
