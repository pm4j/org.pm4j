package org.pm4j.core.pb;

import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmCommand;

public class PbMatcherFactory {

  private PbWidgetFactorySet widgetBinderSet;


  public PbMatcherFactory(PbWidgetFactorySet widgetBinderSet) {
		super();
		this.widgetBinderSet = widgetBinderSet;
	}

  public PbMatcher makeMatcher() {
    PbMatcherMapped map = new PbMatcherMapped();

    // 1. Default Attribute binding configuration: 
    map.addMatcher(new PbMatcherForStrings(widgetBinderSet.getPbText(), widgetBinderSet.getPbTextArea()));
    map.addMatcher(PmAttrBoolean.class, widgetBinderSet.getPbCheckBox());
    map.addMatcher(new PbMatcherForOptions(widgetBinderSet.getPbCombo()));
    
    // XXX: really a spinner for integers? It's nice for a demo. But for real generic forms? 
    map.addMatcher(PmAttrInteger.class, widgetBinderSet.getPbSpinner());

    // Fallback for all other attribute types:
    map.addMatcher(PmAttr.class, widgetBinderSet.getPbText());

    map.addMatcher(PmCommand.class, widgetBinderSet.getPbButton());
    
    map.setDefaultFactory(widgetBinderSet.getPbLabel());

    return map;
  }

}
