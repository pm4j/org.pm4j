package org.pm4j.common.query;

import static org.junit.Assert.assertEquals;
import static org.pm4j.common.pageable.querybased.QueryServiceUtil.findItems;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pm4j.common.pageable.querybased.idquery.IdQueryServiceFake;

/**
 * Demonstrates how a {@link QueryAttrMulti} may be used to query
 *
 * @author oboede
 *
 */
public class QueryAttrMultiTest {

  final QueryService queryService = new QueryService();

  final QueryAttrMulti nameOrDescrAttr = new QueryAttrMulti("nameOrDescr",
      new QueryAttr("name", String.class),
      new QueryAttr("descr", String.class));

  @Before
  public void setUp() {
    queryService.save(new Bean("Anna", "She lives in Berlin."));
    queryService.save(new Bean("Erwin", "He lives in Hamburg. He likes Anna."));
    queryService.save(new Bean("Hugo", "Erwin"));
    queryService.save(new Bean("Max", "Moritz"));
  }

  @Test
  public void findUsingCompOpLike() {
    QueryExprCompare expr = new QueryExprCompare(nameOrDescrAttr, CompOpLike.class, "%likes%");
    assertEquals("[Erwin]", findItems(queryService, expr, 100).toString());;

    expr = new QueryExprCompare(nameOrDescrAttr, CompOpLike.class, "Anna%");
    assertEquals("[Anna]", findItems(queryService, expr, 100).toString());;

    expr = new QueryExprCompare(nameOrDescrAttr, CompOpLike.class, "%Anna%");
    assertEquals("[Anna, Erwin]", findItems(queryService, expr, 100).toString());;

    expr = new QueryExprCompare(nameOrDescrAttr, CompOpLike.class, "%in%");
    assertEquals("[Anna, Erwin, Hugo]", findItems(queryService, expr, 100).toString());;
  }

  @Test
  public void findUsingCompOpIn() {
    QueryExprCompare expr = new QueryExprCompare(nameOrDescrAttr, CompOpIn.class, Arrays.asList("Hugo"));
    assertEquals("[Hugo]", findItems(queryService, expr, 100).toString());;

    expr = new QueryExprCompare(nameOrDescrAttr, CompOpIn.class, Arrays.asList("Max", "Erwin"));
    assertEquals("[Erwin, Hugo, Max]", findItems(queryService, expr, 100).toString());;
  }

  static class Bean {
    public final String name, descr;
    public Bean(String name, String descr) {
      this.name = name;
      this.descr = descr;
    }
    @Override
    public String toString() {
      return name;
    }
  }

  static class QueryService extends IdQueryServiceFake<Bean, String> {
    @Override
    public String getIdForItem(Bean item) {
      return item.name;
    }
  }

}
