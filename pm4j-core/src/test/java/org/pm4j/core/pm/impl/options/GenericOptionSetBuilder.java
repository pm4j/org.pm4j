package org.pm4j.core.pm.impl.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.core.pm.PmOption;
import org.pm4j.core.pm.PmOptionSet;
import org.pm4j.core.pm.annotation.PmOptionCfg;
import org.pm4j.core.pm.annotation.PmOptionCfg.NullOption;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmAttrBase;
import org.pm4j.core.pm.impl.pathresolver.ExpressionPathResolver;
import org.pm4j.core.pm.impl.pathresolver.PathComparatorFactory;
import org.pm4j.core.pm.impl.pathresolver.PathResolver;

/**
 * Algorithm that create a {@link PmOptionSet} from a given set of objects.
 *
 * @author olaf boede
 */
// TODO olaf: move test cases and delete this class.
@Deprecated
class GenericOptionSetBuilder {

  private final PathResolver idPath;
  private final PathResolver namePath;
  private final PathResolver backingValuePath;
  private final NullOption nullOption;
  public final String nullOptionTitleResKey;
  private final PathComparatorFactory sortComparatorFactory;

  /**
   * Creates a builder that puts the given objects to the id, name and value of
   * the generated options.
   */
  public GenericOptionSetBuilder() {
    this("", "", "", NullOption.DEFAULT, "", PmOptionCfg.NOT_SPECIFIED);
  }

  /**
   * Creates a builder that puts the given options to the value of the
   * generated option items.
   *
   * @param idPathString
   *          The path from the item object to the option identifier attribute.
   * @param namePathString
   *          The path from the item object to the option title attribute.
   */
  public GenericOptionSetBuilder(String idPathString, String namePathString) {
    this(idPathString, namePathString, "", NullOption.DEFAULT, "", PmOptionCfg.NOT_SPECIFIED);
  }

  /**
   * @param idPathString
   *          The path from the item object to the option identifier attribute.
   * @param namePathString
   *          The path from the item object to the option title attribute.
   * @param backingValuePathString
   *          The path from the item object to the option value attribute.
   * @param nullOption
   *          Defines if a <code>null</code> option should be generated.
   * @param nullOptionTitleResKey
   *          An optional resource key for the title of the <code>null</code>
   *          option.
   * @param sortOrderSpec TODO
   */
  public GenericOptionSetBuilder(
      String idPathString,
      String namePathString,
      String backingValuePathString,
      NullOption nullOption,
      String nullOptionTitleResKey,
      String sortOrderSpec)
  {
    this.idPath = ExpressionPathResolver.parse(idPathString);
    this.namePath = ExpressionPathResolver.parse(namePathString);
    this.backingValuePath = ExpressionPathResolver.parse(backingValuePathString);
    this.nullOption = nullOption;
    this.nullOptionTitleResKey = StringUtils.defaultIfEmpty(nullOptionTitleResKey, null);
    this.sortComparatorFactory = PmOptionCfg.NOT_SPECIFIED.equals(sortOrderSpec)
          ? null
          : PathComparatorFactory.parse(sortOrderSpec, SyntaxVersion.VERSION_2);
  }

  public List<PmOption> makeOptions(final PmAttrBase<?,?> forAttr, Collection<?> objects) {
    if (objects == null || objects.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      List<PmOption> list = new ArrayList<PmOption>();

      for (Object o : objects) {
        Object id = idPath.getValue(o);
        Object name = namePath.getValue(o);
        Object backingValue = backingValuePath.getValue(o);
        list.add(new PmOptionImpl(
            ObjectUtils.toString(id, ""),
            ObjectUtils.toString(name, ""),
            backingValue));
      }

      if (sortComparatorFactory != null) {
        Collections.sort(list, sortComparatorFactory.getComparator(forAttr));
      }

      // the null option will be added after sorting to prevent
      // sort problems with the null-option.
      if (shouldMakeNullOption(forAttr)) {
        String title = null;
        if (nullOptionTitleResKey != null) {
          title = PmOptionCfg.NULL_OPTION_DEFAULT_RESKEY.equals(nullOptionTitleResKey)
              // default key must not exist.
              ? PmLocalizeApi.findLocalization(forAttr, nullOptionTitleResKey)
              // user-defined key should exist. -> debug-title and a log message will appear.
              : PmLocalizeApi.localize(forAttr, nullOptionTitleResKey);
        }

        List<PmOption> l = new ArrayList<PmOption>(list.size()+1);
        l.add(new PmOptionImpl(null, StringUtils.defaultString(title)));
        l.addAll(list);
        list = l;
      }

      return list;
    }
  }

  private boolean shouldMakeNullOption(PmAttrBase<?,?> forAttr) {
    NullOption nopt = (nullOption == NullOption.DEFAULT)
                      ? forAttr.getNullOptionDefault()
                      : nullOption;

    return nopt == NullOption.YES ||
          (nopt == NullOption.FOR_OPTIONAL_ATTR &&
           ! forAttr.isRequired());
  }


}
