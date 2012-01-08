package org.pm4j.jsf.preprocess;

import org.pm4j.navi.NaviLink;

/**
 * Defines a target to redirect to.
 *
 * @author olaf boede
 */
public class RedirectInfo {

  /** The page to redirect to. */
  public final NaviLink toPage;

  /**
   * Indicates if the application should return to the current page after the
   * user confirmed (or logged in) on the {@link #toPage}.
   */
  public final boolean shouldRedirectBack;

  public RedirectInfo(NaviLink toPage, boolean shouldRedirectBack) {
    this.toPage = toPage;
    this.shouldRedirectBack = shouldRedirectBack;
  }

  @Override
  public String toString() {
    return toPage != null
                ? toPage.toString()
                : "";
  }
}


