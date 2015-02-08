package org.pm4j.demo.basic;

import org.pm4j.core.pm.*;
import org.pm4j.core.pm.annotation.PmAttrCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.impl.*;
import org.pm4j.core.pm.impl.commands.PmCommandNaviBack;

import java.util.Locale;

import static org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO.CLEAR;

public class BasicDemoElementPm extends PmObjectBase {

  @PmAttrCfg(required=true, maxLen=10)
  public final PmAttrString      textFieldShort = new PmAttrStringImpl(this);
  @PmAttrCfg(maxLen=1000)
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

  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand         cmdBack = new PmCommandNaviBack(this);

  @PmCommandCfg(beforeDo=CLEAR)
  public final PmCommand         cmdCancel = new PmCommandImpl(this);

  @PmCommandCfg(beforeDo=CLEAR)
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
