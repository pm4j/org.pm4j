package org.pm4j.demo.basic;

import org.pm4j.core.pm.PmDefaults;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.title.TitleProviderPmResBased;
import org.pm4j.jsf.PmConnectorForJsf;

/**
 * A simple session for JSF demos.
 * Configures some JSF specific stuff.
 */
public class BasicDemoSessionPm extends PmConversationImpl {

	public BasicDemoSessionPm() {
		// FIXME olaf: Try to configure that externally only...
		setPmViewConnector(new PmConnectorForJsf());
		// TODO: Don't use the AsteriskTitleProvider as default...
		PmDefaults.getInstance().setPmAttrTitleProvider(TitleProviderPmResBased.INSTANCE);
	}

}
