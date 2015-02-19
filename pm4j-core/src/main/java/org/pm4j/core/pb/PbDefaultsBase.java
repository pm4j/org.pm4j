package org.pm4j.core.pb;

import org.pm4j.core.pb.PbFactory.ValueUpdateEvent;


/**
 * Base iterface/implementation for presentation binding default settings.
 * 
 * @author olaf boede
 *
 * @param <T_WIDGET_FACTORY_SET> The view platform specific widget set class.
 */
public abstract class PbDefaultsBase <T_WIDGET_FACTORY_SET extends PbWidgetFactorySet> {

  /**
   * Defines the UI event that triggers transfer of entered values to the PM.
   * <p>
   * This may be done on each modification (each click) or on the focus lost
   * event.
   */
  private ValueUpdateEvent valueUpdateEvent = ValueUpdateEvent.MODIFY;

  /**
   * Defines the set of default UI widgets factories used within the application.
   */
  private T_WIDGET_FACTORY_SET widgetFactorySet;

  /**
   * Configures the PM to view matching strategies.<br>
   * It defines the default view representation of PMs.
   * <p>
   * This {@link PbMatcher} defines the default set of {@link PbMatcher}s used
   * for the application.
   */
  private PbMatcher matcher;
  /**
   * Generates the default PM to factory matcher.
   * <p>
   * Each application may define here its own PM to view matching strategy
   * for generic views.
   */
  private PbMatcherFactory matcherFactory;

  public PbDefaultsBase() {
    super();
  }

  public ValueUpdateEvent getValueUpdateEvent() {
    return valueUpdateEvent;
  }

  public void setValueUpdateEvent(ValueUpdateEvent valueUpdateEvent) {
    this.valueUpdateEvent = valueUpdateEvent;
  }

  public PbMatcher getMatcher() {
    if (matcher == null) {
      matcher = getMatcherFactory().makeMatcher();
    }
    return matcher;
  }

  public void setMatcher(PbMatcher pbMatcher) {
    this.matcher = pbMatcher;
  }

  public PbMatcherFactory getMatcherFactory() {
    if (matcherFactory == null) {
      matcherFactory = new PbMatcherFactory(getWidgetFactorySet());
    }
    return matcherFactory;
  }

  public void setMatcherFactory(PbMatcherFactory pbMatcherFactory) {
    this.matcherFactory = pbMatcherFactory;
  }

  public T_WIDGET_FACTORY_SET getWidgetFactorySet() {
    if (widgetFactorySet == null) {
      widgetFactorySet = makeWidgetFactorySet();
    }
    return widgetFactorySet;
  }

  protected abstract T_WIDGET_FACTORY_SET makeWidgetFactorySet();

  public void setWidgetFactorySet(T_WIDGET_FACTORY_SET widgetFactorySet) {
    this.widgetFactorySet = widgetFactorySet;
  }

}