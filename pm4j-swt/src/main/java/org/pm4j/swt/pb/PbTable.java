package org.pm4j.swt.pb;

import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmEvent;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.PmSortOrder;
import org.pm4j.core.util.table.ColSizeSpec;
import org.pm4j.deprecated.core.pm.DeprPmTable;
import org.pm4j.deprecated.core.pm.DeprPmTableCol;
import org.pm4j.deprecated.core.pm.DeprPmTableGenericRow;
import org.pm4j.swt.pb.base.PbViewerToPmBase;
import org.pm4j.swt.util.ColResizeListener;

/**
 * Presentation binder for {@link DeprPmTable} PMs.
 *
 * @author olaf boede
 */
public class PbTable extends PbViewerToPmBase<TableViewer, Table, DeprPmTable> {

  private boolean headerVisble = true;
  private int swtStyle;

  public PbTable() {
    this(SWT.SINGLE | SWT.FULL_SELECTION);
  }

  public PbTable(int swtStyle) {
    this.swtStyle = swtStyle;
  }

  /**
   * Creates a table viewer. It may have a longer live cycle than the {@link Binding}.
   */
  @Override
  protected TableViewer makeViewerImpl(Composite parentCtxt, DeprPmTable pmTable) {
    TableViewer viewer = new TableViewer(parentCtxt, swtStyle);

    viewer.setContentProvider(new ContentProvider());
    viewer.setLabelProvider(new LabelProvider());
    viewer.getTable().setHeaderVisible(headerVisble);

    return viewer;
  }

  @Override
  protected PbBinding makeBinding(DeprPmTable pm) {
    return new Binding();
  }

  /**
   * Handles PM events as well as the SWT modification and focus event.
   */
  public class Binding extends PbViewerToPmBase<TableViewer, Table, DeprPmTable>.Binding {

    /**
     * Creates the required binding between the viewer and the given pm.
     *
     * @param viewer The table viewer that should present the PM
     * @param pm     The PM of the content to display.
     */
    @Override
    public void bind() {
      super.bind();
      Table table = viewer.getTable();

      // Setup the columns.
      table.removeAll();
      List<DeprPmTableCol> columns = ((DeprPmTable)pm).getColumns();
      ColSizeSpec colSpecArr[] = new ColSizeSpec[columns.size()];
      for (int i=0; i<colSpecArr.length; ++i) {
        DeprPmTableCol colPm = columns.get(i);
        makeAndBindTableColumn(viewer, colPm).setData("pm", colPm);
        colSpecArr[i] = colPm.getPmColSize();
      }

      // Add column size control.
      // XXX olaf: the table content is not visible without that listener.
      //  We need to place this listener. But is's still strange that it's required by jFace.
      table.addControlListener(new ColResizeListener(table, colSpecArr));

      // Provide the content to display.
      viewer.setInput(pm);
    }

    /**
     * Gets called on table content change (e.g. on pagination) or sort order change.
     */
    @Override
    protected void onPmValueChange(PmEvent event) {
      viewer.refresh();
    }

    /**
     * Generates a table column and binds it to PM provided UI model information
     * (sort order etc).
     *
     * @param tableViewer The table viewer to add the column to.
     * @param col The PM of the column to add.
     * @return The added STW table column.
     */
    protected TableColumn makeAndBindTableColumn(TableViewer tableViewer, DeprPmTableCol col) {
      TableColumn column = new TableColumn(tableViewer.getTable(), SWT.LEFT);
      column.setText(col.getPmTitle());
      column.setToolTipText(col.getPmTooltip());

      column.addListener(SWT.Selection, new Listener() {
        @Override
        public void handleEvent(Event event) {
          TableColumn col = (TableColumn) event.widget;
          DeprPmTableCol colPm = (DeprPmTableCol) col.getData("pm");
          PmAttrEnum<PmSortOrder> sortOrderAttr = colPm.getSortOrderAttr();
          if (sortOrderAttr.isPmEnabled()) {
            Table table = col.getParent();

            TableColumn prevSortCol = table.getSortColumn();
            if (prevSortCol != col) {
              if (prevSortCol != null) {
                DeprPmTableCol prevSortColPm = (DeprPmTableCol) prevSortCol.getData("pm");
                prevSortColPm.getSortOrderAttr().setValue(PmSortOrder.NEUTRAL);
              }

              table.setSortColumn(col);
            }

            PmSortOrder sortOrder = sortOrderAttr.getValue();
            switch (sortOrder) {
              case ASC:   sortOrderAttr.setValue(PmSortOrder.DESC);
                          // TODO: should be done in automatically in response to the setValue event.
                          table.setSortDirection(SWT.DOWN);
                          break;
              case DESC:  sortOrderAttr.setValue(PmSortOrder.NEUTRAL);
                          table.setSortDirection(SWT.NONE);
                          break;
              case NEUTRAL:  sortOrderAttr.setValue(PmSortOrder.ASC);
                          table.setSortDirection(SWT.UP);
                          break;
              default: throw new PmRuntimeException(sortOrderAttr,
                          "unknown sort order enum value: " + sortOrder);
            }
          }
        }
      });

      return column;
    }

  }


  /**
   * Provides the rows of a {@link DeprPmTable}.
   */
  public static class ContentProvider implements IStructuredContentProvider {

    private static final Object[] EMPTY_ARRAY = {};

    @Override
    public Object[] getElements(Object inputElement) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      List<DeprPmTableGenericRow<?>> list = ((DeprPmTable)inputElement).getGenericRows();
      return list != null
          ? list.toArray()
          : EMPTY_ARRAY;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }

  /**
   * Provides the table cell values and icons for a table.
   */
  public class LabelProvider implements ITableLabelProvider {

    private ImageRegistry imageRegistry;

    public LabelProvider() {
    }

    public String getColumnText(Object rowObj, int column_index) {
      String text = null;
      if (rowObj instanceof DeprPmTableGenericRow) {
        PmObject pm = getCellPm(rowObj, column_index);

        if (pm instanceof PmAttr<?>) {
          text = ((PmAttr<?>)pm).getValueAsString();
        }
      }
      return text;
    }

    public void addListener(ILabelProviderListener ilabelproviderlistener) {
    }

    public void dispose() {
    }

    public boolean isLabelProperty(Object obj, String s) {
      return false;
    }

    public void removeListener(ILabelProviderListener ilabelproviderlistener) {
    }

    public Image getColumnImage(Object element, int colIdx) {
      if (imageRegistry == null || element == null)
        return null;

      String iconPath = getCellPm(element, colIdx).getPmIconPath();

      return iconPath != null
          ? imageRegistry.get(iconPath)
          : null;
    }

    public ImageRegistry getImageRegistry() {
      return imageRegistry;

    }

    public void setImageRegistry(ImageRegistry imageRegistry) {
      this.imageRegistry = imageRegistry;
    }

    private PmObject getCellPm(Object rowPm, int colIdx) {
      return ((DeprPmTableGenericRow)rowPm).getCell(colIdx);
    }

  }

}
