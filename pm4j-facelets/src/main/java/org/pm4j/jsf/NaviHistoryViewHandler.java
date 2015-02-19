package org.pm4j.jsf;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.jsf.util.NaviJsfUtil;
import org.pm4j.jsf.util.ViewHandlerWrapper;
import org.pm4j.navi.NaviHistory;

/**
 * Creates Action URLs with navigation history URL parameters.
 *
 * @author olaf boede
 */
// TODO olaf: move to navi package
public class NaviHistoryViewHandler extends ViewHandlerWrapper {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(NaviHistoryViewHandler.class);

	private ViewHandler base;

	public NaviHistoryViewHandler(ViewHandler base) {
		assert base != null;
		this.base = base;
	}

	@Override
	protected ViewHandler getWrapped() {
		return base;
	}

	@Override
	public String getActionURL(FacesContext context, String viewId) {
		String url = super.getActionURL(context, viewId);

		if (url != null) {
			NaviHistory h = NaviJsfUtil.getNaviHistory();
			if (h != null) {
				url += ((url.indexOf('?') == -1) ? '?' : '&') +
					   h.getNaviCfg().getVersionParamName() + '=' + h.getVersionString();
			}
		}

		return url;
	}

}
