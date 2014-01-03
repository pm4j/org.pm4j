package org.pm4j.common.pageable.querybased.pagequery;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.pm4j.common.query.QueryParams;

/**
 * Provides a selection containing all items provided by the service.
 *
 * @param <T_ITEM>
 * @param <T_ID>
 *
 * @author olaf boede
 */
// TODO oboede: currently unused.
public class PageQueryAllItemsSelection<T_ITEM, T_ID extends Serializable> extends PageQuerySelectionBase<T_ITEM, T_ID>{

  private static final long serialVersionUID = 1L;

  private final QueryParams queryParams;
  private int pageSize = 20;

  public PageQueryAllItemsSelection(PageQueryService<T_ITEM, T_ID> service) {
    this(service, null);
  }


  public PageQueryAllItemsSelection(PageQueryService<T_ITEM, T_ID> service, QueryParams queryParams) {
    super(service);
    assert pageSize > 0;

    this.queryParams = (queryParams != null)
        ? queryParams
        : new QueryParams();
  }

  @Override
  public long getSize() {
    return getPageableQueryService().getItemCount(queryParams);
  }

  @Override
  public boolean contains(T_ITEM item) {
    return true;
  }

  @Override
  public Iterator<T_ITEM> iterator() {
    return new PageQueryItemIteratorBase<T_ITEM>(pageSize) {
      @Override
      protected List<T_ITEM> getItems(long startIdx, int blockSize) {
        return getPageableQueryService().getItems(queryParams, startIdx, blockSize);
      }
    };
  }

  @Override
  public void setIteratorBlockSizeHint(int readBlockSize) {
    assert readBlockSize > 0;
    pageSize = readBlockSize;
  }

  protected PageQueryService<T_ITEM, T_ID> getPageableQueryService() {
    return (PageQueryService<T_ITEM, T_ID>) getService();
  }

}
