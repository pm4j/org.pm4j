package org.pm4j.core.pm.impl.title;

import org.pm4j.core.pm.PmConstants;
import org.pm4j.core.pm.api.PmLocalizeApi;
import org.pm4j.core.pm.impl.PmObjectBase;

/**
 * Provides titles based on presentation model resource keys.
 *
 * @author Olaf Boede
 */
public class TitleProviderPmResBased<T extends PmObjectBase> implements PmTitleProvider<T> {

  /**
   * An instance that can be used as a singleton.
   */
  public static final TitleProviderPmResBased<PmObjectBase> INSTANCE = new TitleProviderPmResBased<PmObjectBase>();

  /**
   * @return <code>false</code>.
   */
  @Override
  public boolean canSetTitle(T item) {
    return false;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public void setTitle(T item, String titleString) {
    throw new UnsupportedOperationException("Title can't be changed. Instance: " + item + "\n\tUsed title provider: "
        + getClass());
  }

  @Override
  public String getTitle(T item) {
    return PmLocalizeApi.localize(item, item.getPmResKey());
  }

  @Override
  public String getShortTitle(T item) {
    return PmLocalizeApi.findLocalization(item, item.getPmResKey() + PmConstants.RESKEY_POSTFIX_SHORT_TITLE);
  }

  @Override
  public String getToolTip(T item) {
    return PmLocalizeApi.findLocalization(item, item.getPmResKey() + PmConstants.RESKEY_POSTFIX_TOOLTIP);
  }

  /**
   * Provides an icon resource string that is defined within the resource file(s).
   * <p>
   * For enabled items a resource string with the postfix '_icon' will be used.<br>
   * For disabled item a resource string with the postfix '_iconDisabled' will be used.<br>
   * If there is no '.icon_disabled' resource defined, the '_icon' resource will be used
   * for the disabled state too.
   */
  public String getIconPath(T item) {
    if (item.isPmEnabled()) {
      String resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON;
      return PmLocalizeApi.findLocalization(item, resKey);
    }
    else {
      String resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON_DISABLED;
      String path = PmLocalizeApi.findLocalization(item, resKey);

      // Fallback: Use the standard icon for the disabled state too when there is no specific
      //           icon path defined.
      if (path == null) {
        resKey = item.getPmResKey() + PmConstants.RESKEY_POSTFIX_ICON;
        path = PmLocalizeApi.findLocalization(item, resKey);
      }

      return path;
    }
  }

}
