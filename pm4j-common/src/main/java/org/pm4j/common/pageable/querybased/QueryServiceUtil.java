package org.pm4j.common.pageable.querybased;

import java.util.List;

import org.pm4j.common.pageable.querybased.idquery.IdQueryService;
import org.pm4j.common.pageable.querybased.pagequery.PageQueryService;
import org.pm4j.common.query.QueryExpr;
import org.pm4j.common.query.QueryParams;
import org.pm4j.common.util.collection.ListUtil;

/**
 * The {@link QueryService} and it's sub classes are designed to support
 * pageable table or list models.<br>
 * This utility provides helper methods that make it easier to use these
 * services for other tasks too (e.g. to provide a plain list).
 *
 * @author oboede
 */
public class QueryServiceUtil {

  private QueryServiceUtil() {
  }

  /**
   * Provides a single item that matches the given expression criteria.
   * <p>
   * Throws an {@link IllegalArgumentException} if no or more than one item was
   * found for the given criteria.
   *
   * @param s
   *          The service to use.<br>
   *          Should be a kind of {@link PageQueryService} or
   *          {@link IdQueryService}.
   * @param itemExpr
   *          The expression that identifies the object to find.
   * @return The corresponding instance. Never <code>null</code>.
   */
  public static <T> T getItem(QueryService<T, ?> s, QueryExpr itemExpr) {
    T item = findItem(s, itemExpr);
    if (item == null) {
      throw new IllegalArgumentException("No item found for expression " + itemExpr);
    }
    return item;
  }

  /**
   * Searches for a single item that matches the given expression criteria.
   * <p>
   * Throws an {@link IllegalArgumentException} if more than one item was found
   * for the given search criteria.
   *
   * @param s
   *          The service to use.<br>
   *          Should be a kind of {@link PageQueryService} or
   *          {@link IdQueryService}.
   * @param itemExpr
   *          The expression that identifies the object to find.
   * @return The found instance or <code>null</code>.
   */
  public static <T> T findItem(QueryService<T, ?> s, QueryExpr itemExpr) {
    // Fetches internally max. two items to be able to detect that the expression finds more
    // than one item. In that case an IllegalArgumentException will be thrown.
    List<T> items = findItems(s, itemExpr, 2);
    if (items.size() > 1) {
      throw new IllegalArgumentException("More than one item found for expression " + itemExpr);
    }
    return ListUtil.listToItemOrNull(items);
  }

  /**
   * Searches for items that matches the given expression criteria.
   *
   * @param s
   *          The service to use.<br>
   *          Should be a kind of {@link PageQueryService} or
   *          {@link IdQueryService}.
   * @param itemExpr
   *          The expression that identifies the objects to find.
   * @param maxNumOfItems
   *          The maximum number of items to retrieve.
   * @return The list of found items. Never <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> findItems(QueryService<T, ?> s, QueryExpr itemExpr, int maxNumOfItems) {
    QueryParams qp = new QueryParams();
    qp.setQueryExpression(itemExpr);
    if (s instanceof PageQueryService) {
      List<T> items = ((PageQueryService<T, ?>) s).getItems(qp, 0, maxNumOfItems);
      return items;
    } else if (s instanceof IdQueryService) {
      IdQueryService<T, Object> qs = (IdQueryService<T, Object>) s;
      List<Object> ids = qs.findIds(qp, 0, maxNumOfItems);
      List<T> items = qs.getItems(ids);
      return items;
    } else {
      throw new RuntimeException("Unsupported service type found: " + s);
    }
  }

}
