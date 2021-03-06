package org.pm4j.core.pm;

import static org.apache.commons.lang.Validate.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pm4j.common.exception.CheckedExceptionWrapper;
import org.pm4j.common.expr.Expression.SyntaxVersion;
import org.pm4j.common.query.filter.FilterDefinitionFactory;
import org.pm4j.common.query.filter.FilterDefinitionFactoryImpl;
import org.pm4j.core.pm.annotation.PmCommandCfg;
import org.pm4j.core.pm.annotation.PmCommandCfg.BEFORE_DO;
import org.pm4j.core.pm.annotation.PmInject;
import org.pm4j.core.pm.impl.PmCommandImpl;
import org.pm4j.core.pm.impl.inject.DiResolverFactory;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectFieldByExpression;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectFieldByParentOfType;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectSetterByExpression;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectSetterByParentOfType;
import org.pm4j.core.pm.impl.title.PmTitleProvider;
import org.pm4j.core.pm.impl.title.TitleProviderPmResBased;

/**
 * A singleton that knows about the application wide defined default
 * presentation model implementation strategies.
 *
 * @author Olaf Boede
 */
@SuppressWarnings("rawtypes")
public class PmDefaults implements Cloneable {

  /**
   * The singleton.
   */
  private static PmDefaults instance;

  /**
   * Defines the used default title provider for PMs.<br>
   * Default value for this attribute: {@link TitleProviderPmResBased}.
   */
  private PmTitleProvider pmTitleProvider = TitleProviderPmResBased.INSTANCE;

  /**
   * The application specific filter compare definition factory. Used for table filters.
   */
  private FilterDefinitionFactory filterCompareDefinitionFactory = new FilterDefinitionFactoryImpl.DefaultFactory();

  /**
   * The delimiter that is used to terminate multiple format strings in resource string definitions.
   * An example:
   * <pre>myPm.myDateAttr_format=d/M/yy|dd/MM/yyyy<pre>
   * This defines two accepted input formats. (The last one will be used as output format.)
   * <p>
   * The default delimiter is the pipe symbol '|'.
   */
  private String multiFormatPatternDelimiter = "|";

  /**
   * Defines the default {@link PmCommandCfg#beforeDo()} setting for commands
   * that do not define this attribute explicitly.
   * <p>
   * Only used for the old {@link PmCommandImpl}.
   */
  private PmCommandCfg.BEFORE_DO[] beforeDoCommandDefault = new BEFORE_DO[] {BEFORE_DO.VALIDATE};

  /**
   * Temporary switch. Will be removed when the new behavior is rolled out to all dialogs.
   */
  private boolean exceptionOnSwitchToDisabledTab = true;

  @Deprecated // no replacement planned
  public boolean debugHints = false;

  /**
   * The set of dependency injection resolvers used for the application.
   * <p>
   * The default implementation supports {@link PmInject}.
   */
  private DiResolverFactory[] diResolverFactories = {
      new DiResolverFactoryPmInjectFieldByExpression(),
      new DiResolverFactoryPmInjectSetterByExpression(),
      new DiResolverFactoryPmInjectFieldByParentOfType(),
      new DiResolverFactoryPmInjectSetterByParentOfType()
  };

  /**
   * Controls if a not resolvable dependency injection leads to an exception or
   * will be accepted silently.<br>
   * It is recommended to leave the default value <code>false</code> for production
   * environments.
   */
  private boolean diResolverNullCheckLenient = false;

  /**
   * The expression syntax may be configured here globally or per conversation.
   */
  private SyntaxVersion expressionSyntaxVersion = SyntaxVersion.VERSION_2;

  @Override
  public PmDefaults clone() {
    try {
      return (PmDefaults) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new CheckedExceptionWrapper(e);
    }
  }

  /**
   * @return The application wide default strategies.
   */
  public static PmDefaults getInstance() {
    if (instance == null) {
      instance = new PmDefaults();
    }
    return instance;
  }

  /**
   * Defines a new set of default strategies.
   *
   * @param newInstance The new definition.
   */
  public static void setInstance(PmDefaults newInstance) {
    instance = newInstance;
  }

  // -- getter / setter --

  /**
   * @return the set of dependency injection resolvers used for the application.
   */
  public DiResolverFactory[] getDiResolverFactories() {
    return diResolverFactories;
  }

  /**
   * Allows to configure the application specific set of dependency injection mechanisms.
   *
   * @param diResolverFactories the set of dependency injection resolvers used for the application.
   */
  public void setDiResolverFactories(DiResolverFactory[] diResolverFactories) {
    this.diResolverFactories = diResolverFactories;
  }


  /**
   * Adds a dependency injection resolver factory.
   *
   * @param diResolverFactory the new factory to add.
   */
  public void addDiResolverFactory(DiResolverFactory diResolverFactory) {
    notNull(diResolverFactory);

    int oldSize = diResolverFactories.length;
    diResolverFactories = Arrays.copyOf(diResolverFactories, oldSize + 1);
    diResolverFactories[oldSize] = diResolverFactory;
  }


  public PmTitleProvider getPmTitleProvider() {
    return pmTitleProvider;
  }

  public void setPmTitleProvider(PmTitleProvider pmTitleProvider) {
    notNull(pmTitleProvider);
    this.pmTitleProvider = pmTitleProvider;
  }

  public Set<PmCommandCfg.BEFORE_DO> getBeforeDoCommandDefault() {
    return new HashSet<PmCommandCfg.BEFORE_DO>(Arrays.asList(beforeDoCommandDefault));
  }

  public void setBeforeDoCommandDefault(PmCommandCfg.BEFORE_DO... beforeDoCommandDefault) {
    this.beforeDoCommandDefault = beforeDoCommandDefault;
  }

  public String getMultiFormatPatternDelimiter() {
    return multiFormatPatternDelimiter;
  }

  public void setMultiFormatPatternDelimiter(String multiFormatPatternDelimiter) {
    this.multiFormatPatternDelimiter = multiFormatPatternDelimiter;
  }

  public FilterDefinitionFactory getFilterCompareDefinitionFactory() {
    return filterCompareDefinitionFactory;
  }

  public void setFilterCompareDefinitionFactory(FilterDefinitionFactory filterCompareDefinitionFactory) {
    this.filterCompareDefinitionFactory = filterCompareDefinitionFactory;
  }

  /**
   * @return the expressionSyntaxVersion
   */
  public SyntaxVersion getExpressionSyntaxVersion() {
    return expressionSyntaxVersion;
  }

  /**
   * @param expressionSyntaxVersion the expressionSyntaxVersion to set
   */
  public void setExpressionSyntaxVersion(SyntaxVersion expressionSyntaxVersion) {
    this.expressionSyntaxVersion = expressionSyntaxVersion;
  }

  /**
   * @return the diResolverNullCheck
   */
  public boolean isDiResolverNullCheckLenient() {
    return diResolverNullCheckLenient;
  }

  /**
   * @param diResolverNullCheck the diResolverNullCheck to set
   */
  public void setDiResolverNullCheckLenient(boolean diResolverNullCheck) {
    this.diResolverNullCheckLenient = diResolverNullCheck;
  }

  /**
   * @return the exceptionOnSwitchToDisabledTab
   */
  public boolean isExceptionOnSwitchToDisabledTab() {
    return exceptionOnSwitchToDisabledTab;
  }

  /**
   * @param exceptionOnSwitchToDisabledTab the exceptionOnSwitchToDisabledTab to set
   */
  public void setExceptionOnSwitchToDisabledTab(boolean exceptionOnSwitchToDisabledTab) {
    this.exceptionOnSwitchToDisabledTab = exceptionOnSwitchToDisabledTab;
  }

}
