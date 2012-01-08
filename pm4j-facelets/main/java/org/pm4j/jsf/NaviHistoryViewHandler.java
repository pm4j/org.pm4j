package org.pm4j.jsf;

import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
  private static final Log log = LogFactory.getLog(NaviHistoryViewHandler.class);

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
