package org.pm4j.core.pm.impl.title;

import org.pm4j.core.pm.impl.PmAttrBase;

/**
 * Provides an asterisk (*) decoration for required attributes.
 *
 * @author olaf boede
 */
public class AsteriskAttrTitleProvider extends TitleProviderPmResBased<PmAttrBase<?,?>> {

  @Override
  public String getTitle(PmAttrBase<?,?> item) {
    return item.isRequired()
      ? super.getTitle(item) + " *"
      : super.getTitle(item);
  }
}
