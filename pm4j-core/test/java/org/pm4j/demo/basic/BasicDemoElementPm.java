package org.pm4j.demo.basic;

import java.util.Locale;

import org.pm4j.core.pm.PmAttrBoolean;
import org.pm4j.core.pm.PmAttrDate;
import org.pm4j.core.pm.PmAttrDouble;
import org.pm4j.core.pm.PmAttrEnum;
import org.pm4j.core.pm.PmAttrInteger;
import org.pm4j.core.pm.PmAttrLong;
import org.pm4j.core.pm.PmAttrString;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmAttrStringCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.impl.PmAttrBooleanImpl;
import org.pm4j.core.pm.impl.PmAttrDateImpl;
import org.pm4j.core.pm.impl.PmAttrDoubleImpl;
import org.pm4j.core.pm.impl.PmAttrEnumImpl;
import org.pm4j.core.pm.impl.PmAttrIntegerImpl;
import org.pm4j.core.pm.impl.PmAttrLongImpl;
import org.pm4j.core.pm.impl.PmAttrStringImpl;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.PmElementImpl;
import org.pm4j.core.pm.impl.commands.PmCommandNaviBack;

public class BasicDemoElementPm extends PmElementImpl {

  @PmAttrStringCfg(maxLen=10) @PmAttrCfg(required=true)
  public final PmAttrString      textFieldShort = new PmAttrStringImpl(this);
  @PmAttrStringCfg(maxLen=1000)
  public final PmAttrString      textFieldLong = new PmAttrStringImpl(this);
  @PmAttrCfg(required=true)
  public final PmAttrInteger     intField = new PmAttrIntegerImpl(this);
  public final PmAttrLong        longField = new PmAttrLongImpl(this);
  public final PmAttrDouble      doubleField = new PmAttrDoubleImpl(this);
  public final PmAttrDate        dateField = new PmAttrDateImpl(this);
  public final PmAttrBoolean     booleanField = new PmAttrBooleanImpl(this);
  public enum Color { RED, GREEN, BLUE };
  @PmAttrCfg(defaultValue="GREEN")
  public final PmAttrEnum<Color> color = new PmAttrEnumImpl<Color>(this, Color.class);

  // .. persist the values somehow.
  public final PmCommand         cmdSave = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() throws Exception {
      // ... persist the changed values.
      navigateBack();
    }
  };

  @PmCommandCfg(requiresValidValues=false)
  public final PmCommand         cmdBack = new PmCommandNaviBack(this);

  @PmCommandCfg(requiresValidValues=false)
  public final PmCommand         cmdCancel = new PmCommandImpl(this);

  @PmCommandCfg(requiresValidValues=false)
  public final PmCommand         cmdSwitchLanguage = new PmCommandImpl(this) {
    @Override
    protected void doItImpl() {
      PmConversation s = getPmConversation();
      Locale l = s.getPmLocale().equals(Locale.ENGLISH)
                    ? Locale.GERMAN : Locale.ENGLISH;
      s.setPmLocale(l);
    }
  };

  /** Default constructor for DI frameworks that support setters liks JSF */
  public BasicDemoElementPm() {
    super();
  }

  /** Initializing constructor */
  public BasicDemoElementPm(PmObject pmParent) {
    super(pmParent);
  }

	// -- Generated getters. Sorry for that unnecessary overhead.
  // But unfortunately JSF can't access public fields :-( --

  public PmAttrString getTextFieldShort() {
    return textFieldShort;
  }

  public PmAttrString getTextFieldLong() {
    return textFieldLong;
  }

  public PmAttrInteger getIntField() {
    return intField;
  }

  public PmAttrLong getLongField() {
    return longField;
  }

  public PmAttrDouble getDoubleField() {
    return doubleField;
  }

  public PmAttrDate getDateField() {
    return dateField;
  }

  public PmCommand getCmdCancel() {
    return cmdCancel;
  }

  public PmCommand getCmdSave() {
    return cmdSave;
  }

  public PmAttrEnum<Color> getColor() {
    return color;
  }

  public PmAttrBoolean getBooleanField() {
    return booleanField;
  }

  public PmCommand getCmdBack() {
    return cmdBack;
  }

  public PmCommand getCmdSwitchLanguage() {
    return cmdSwitchLanguage;
  }
}
