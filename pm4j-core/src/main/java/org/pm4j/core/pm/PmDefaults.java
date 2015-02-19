package org.pm4j.core.pm;

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
import org.pm4j.core.pm.impl.PmObjectBase.NameBuilder;
import org.pm4j.core.pm.impl.PmObjectBase.NameBuilderRelNameWithHashCode;
import org.pm4j.core.pm.impl.inject.DiResolverFactory;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectFieldByExpression;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectFieldByParentOfType;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectSetterByExpression;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmInjectSetterByParentOfType;
import org.pm4j.core.pm.impl.inject.DiResolverFactoryPmProduces;
import org.pm4j.core.pm.impl.title.AsteriskAttrTitleProvider;
import org.pm4j.core.pm.impl.title.PmTitleProvider;
import org.pm4j.core.pm.impl.title.TitleProviderPmResBased;

/**
 * A singleton that knows about the application wide defined default
 * presentation model implementation strategies.
 *
 * @author olaf boede
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
   * Defines the used default title provider for {@link PmAttr}'s.<br>
   * Some applications use specific title provider for attributes to reflect the
   * changed state of attributes (see {@link AsteriskAttrTitleProvider}).<br>
   * Default value for this attribute: {@link TitleProviderPmResBased}.
   */
  private PmTitleProvider pmAttrTitleProvider = TitleProviderPmResBased.INSTANCE;

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
   * Defines the algorithm used for the toString() implementation of PMs.<br>
   * The default implementation provides a name that reflects the complete PM hierarchy.
   */
  private NameBuilder toStringNameBuilder = NameBuilderRelNameWithHashCode.INSTANCE;

  /**
   * Defines the algorithm used for building the name of PMs as it will appear in the logs.<br>
   * The default implementation provides a name that reflects the complete PM hierarchy.
   */
  private NameBuilder logStringBuilder = NameBuilderRelNameWithHashCode.INSTANCE;

  /**
   * Defines the default {@link PmCommandCfg#beforeDo()} setting for commands
   * that do not define this attribute explicitly.
   */
  private PmCommandCfg.BEFORE_DO[] beforeDoCommandDefault = new BEFORE_DO[] {BEFORE_DO.VALIDATE};
  
  /**
   * Defines, if validation error messages should be added to the tool tip text of
   * the affected attributes.
   */
  public boolean addErrorMessagesToTooltip = true;

  public boolean debugHints = false;

  /**
   * The default defines that a factory must be declared on the PM that uses the factory.
   * E.g. a PmTable should declare the factory for its row PMs directly.
   * <p>
   * If this property is set to <code>true</code>, a factory in the parent hierarchy may be
   * used also.<br>
   * This hierarchy feature will be removed in one of the next releases.
   */
  public boolean supportFactoryHierarchy = false;

  /**
   * The set of dependency injection resolvers used for the application.
   * <p>
   * The default implementation supports {@link PmInject}.
   */
  private DiResolverFactory[] diResolverFactories = {
      // Producer factories need to be placed in front of injection resolver factories.
      new DiResolverFactoryPmProduces(),
      new DiResolverFactoryPmInjectFieldByExpression(),
      new DiResolverFactoryPmInjectSetterByExpression(),
      new DiResolverFactoryPmInjectFieldByParentOfType(),
      new DiResolverFactoryPmInjectSetterByParentOfType()
  };

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
    assert diResolverFactory != null;

    int oldSize = diResolverFactories.length;
    diResolverFactories = Arrays.copyOf(diResolverFactories, oldSize + 1);
    diResolverFactories[oldSize] = diResolverFactory;
  }


  public PmTitleProvider getPmTitleProvider() {
    return pmTitleProvider;
  }

  public void setPmTitleProvider(PmTitleProvider pmTitleProvider) {
    assert pmTitleProvider != null;

    this.pmTitleProvider = pmTitleProvider;
  }

  public PmTitleProvider getPmAttrTitleProvider() {
    return pmAttrTitleProvider;
  }

  public void setPmAttrTitleProvider(PmTitleProvider pmAttrTitleProvider) {
    this.pmAttrTitleProvider = pmAttrTitleProvider;
  }

  public NameBuilder getToStringNameBuilder() {
    return toStringNameBuilder;
  }

  public void setToStringNameBuilder(NameBuilder toStringNameBuilder) {
    this.toStringNameBuilder = toStringNameBuilder;
  }

  public NameBuilder getLogStringBuilder() {
    return logStringBuilder;
  }

  public void setLogStringBuilder(NameBuilder logStringBuilder) {
    this.logStringBuilder = logStringBuilder;
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

}
