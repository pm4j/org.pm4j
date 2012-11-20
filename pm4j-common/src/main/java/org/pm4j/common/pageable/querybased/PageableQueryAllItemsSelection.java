package org.pm4j.common.pageable.querybased;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.query.QueryParams;
import org.pm4j.common.selection.Selection;

/**
 * Provides a selection containing all items provided by the service.
 *
 * @param <T_ITEM>
 * @param <T_ID>
 *
 * @author olaf boede
 */
public class PageableQueryAllItemsSelection<T_ITEM, T_ID extends Serializable> extends PageableQuerySelectionBase<T_ITEM, T_ID>{

  private static final long serialVersionUID = 1L;

  private final QueryParams queryParams;
  private int pageSize = 20;

  public PageableQueryAllItemsSelection(PageableQueryService<T_ITEM, T_ID> service) {
    this(service, null);
  }


  public PageableQueryAllItemsSelection(PageableQueryService<T_ITEM, T_ID> service, QueryParams queryParams) {
    super(service);
    assert pageSize > 0;

    this.queryParams = (queryParams != null)
        ? queryParams
        : new QueryParams();
  }

  @Override
  public long getSize() {
    return getService().getItemCount(queryParams);
  }

  @Override
  public boolean contains(T_ITEM item) {
    return true;
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new PageableItemIteratorBase<T_ITEM>(pageSize) {
      @Override
      protected List<T_ITEM> getItems(long startIdx, int blockSize) {
        return getService().getItems(queryParams, startIdx, blockSize);
      }
    };
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    assert readBlockSize > 0;
    pageSize = readBlockSize;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T_BEAN> Selection<T_BEAN> getBeanSelection() {
    return (Selection<T_BEAN>) this;  }

}
