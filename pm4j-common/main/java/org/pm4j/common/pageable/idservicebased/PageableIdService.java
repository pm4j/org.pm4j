package org.pm4j.common.pageable.idservicebased;

import java.util.List;

import org.pm4j.common.query.Query;
import org.pm4j.common.selection.ItemIdConverter;

public interface PageableIdService<T_BEAN, T_ID> extends ItemIdConverter<T_BEAN, T_ID>{

  List<T_ID> findIds(Query query);

  List<T_BEAN> getItems(List<T_ID> ids);

  int getUnfilteredItemCount(Query query);
}
