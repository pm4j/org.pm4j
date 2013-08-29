package org.pm4j.core.pm.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.pm4j.core.exception.PmConverterException;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmAttr;
import org.pm4j.core.pm.PmCommand;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmDataInput;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmCacheApi;
import org.pm4j.core.pm.impl.PmObjectBase.PmInitState;
import org.pm4j.core.pm.impl.commands.PmCommandSeparator;

/**
 * Some convenience algorithms that may be used often but are not (yet?) members
 * of the PM interfaces.
 *
 * @author olaf boede
 */
public final class PmUtil {

  /**
   * Generates a list that represents the PM hierarchy of the given PM.
   * <p>
   * The first item is the given PM itself.<br>
   * The last item is the highest one of the reported PM hierarchy..
   *
   * @param pm
   *          The PM to analyze.
   * @param withSessionHierarchy
   *          If <code>true</code> PM's till the root session will be returned.<br>
   *          If <code>false</code> PM's till the session (exclusive) that
   *          manages the given PM will returned.
   * @return The session hierarchy.
   */
  public static List<PmObject> getPmHierarchy(PmObject pm, boolean withSessionHierarchy) {
    List<PmObject> list = new ArrayList<PmObject>();
    PmConversation pmConversation = pm.getPmConversation();
    PmObject p = pm;
    do {
      if (!withSessionHierarchy) {
        // Terminate when the session of the item is reached.
        if (p == pmConversation) {
          break;
        }
      }

      list.add(p);
      p = p.getPmParent();
    } while (p != null);

    return list;
  }

  /**
   * Provides the root of the session hierarchy for the given PM.
   *
   * @param pm
   *          The PM to get the root session for.
   * @return The root session instance.
   */
  public static PmConversation getRootSession(PmObject pm) {
    PmConversation s = pm.getPmConversation();
    while (s.getPmParentConversation() != null) {
      s = s.getPmParentConversation();
    }
    return s;
  }

  /**
   * Provides the subset of visible PMs.
   *
   * @param allItems
   *          The collection to filter.
   * @return The visible items of the given collection.
   */
  public static List<? extends PmObject> getVisibleItems(Collection<? extends PmObject> allItems) {
    List<PmObject> visibleItems = new ArrayList<PmObject>();
    for (PmObject pm : allItems) {
      if (pm.isPmVisible()) {
        visibleItems.add(pm);
      }
    }
    return visibleItems;
  }

  /**
   * Searches an instance of the requested type within the PM context hierarchy
   * of the given PM.
   *
   * @param startPm
   *          The PM to get the context object for.
   * @param type
   *          The type of the requested context object.
   * @return The found instance or <code>null</code> if there was not context
   *         object with the given type.
   */
  @SuppressWarnings("unchecked")
  public static <T> T findPmParentOfType(PmObject startPm, Class<T> type) {
    PmObject pm = startPm.getPmParent();
    while (pm != null) {
      if (type.isAssignableFrom(pm.getClass())) {
        return (T) pm;
      }
      pm = pm.getPmParent();
    }
    // No context object for the requested type found.
    return null;
  }

  /**
   * Gets the PM parent with the requested type from the PM hierarchy
   * of the given PM.
   *
   * @param startPm
   *          The PM to get the context object for.
   * @param type
   *          The type of the requested context object.
   * @return The found parent instance.
   * @throws PmRuntimeException if there is no parent PM with the given type.
   */
  public static <T> T getPmParentOfType(PmObject startPm, Class<T> type) {
    T pm = findPmParentOfType(startPm, type);
    if (pm == null) {
      throw new PmRuntimeException(startPm, "Can't find a parent PM of type '" + type + "'.");
    }
    return pm;
  }


  /**
   * Converts an array of cache kinds defintions to a set.
   * <p>
   * If no argument is provided, the {@link PmObject.CacheKind#ALL_SET}
   * will be returned.
   *
   * @param cacheKinds
   *          The array to convert.
   * @return The corresponding set.
   */
  public static final Set<PmCacheApi.CacheKind> cacheKindArrayToSet(PmCacheApi.CacheKind... cacheKinds) {
    Set<PmCacheApi.CacheKind> set = null;
    if (cacheKinds.length == 0) {
      set = PmCacheApi.CacheKind.ALL_SET;
    }
    else {
      set = new HashSet<PmCacheApi.CacheKind>(cacheKinds.length);
      for (PmCacheApi.CacheKind c : cacheKinds) {
        if (c == PmCacheApi.CacheKind.ALL) {
          set = PmCacheApi.CacheKind.ALL_SET;
          break;
        }
        else {
          set.add(c);
        }
      }
    }

    return set;
  }

  /**
   * @return The set of all child PM's. Includes field bound PM's as well as
   *         dynamically created PM's.
   */
  public static List<PmObject> getPmChildren(PmObject pm) {
    return ((PmObjectBase)pm).getPmChildren();
  }

  /**
   * Searches for a direct child PM.<br>
   * Finds PM's declared in public fields as well as PM's that where added
   * dynamically.
   *
   * @param localName
   *          Name of the child within this instance.
   * @return The found child PM or <code>null</code>.
   */
  public static PmObject findChildPm(PmObject pm, String localName) {
    return ((PmObjectBase)pm).findChildPm(localName);
  }

  /**
   * @param pm The parent to get the direct child PMs for.
   * @param childClass Type of the requested children.
   * @return The set of all child PM's of the given type. Includes field bound PM's as well as
   *         dynamically created PM's.
   */
  public static <T extends PmObject> List<T> getPmChildrenOfType(PmObject pm, Class<T> childClass) {
    List<PmObject> allChildren = ((PmObjectBase)pm).getPmChildren();
    if (allChildren.isEmpty()) {
      return Collections.emptyList();
    }
    else {
      List<T> children = new ArrayList<T>();
      for (PmObject c : allChildren) {
        if (childClass.isAssignableFrom(c.getClass())) {
          children.add(childClass.cast(c));
        }
      }
      return children;
    }
  }

  /**
   * Determines if the given instances are participants of a parent child relation.
   *
   * @param parentCandidate The parent candidate.
   * @param childCandidate The child candidate.
   * @return <code>true</code> if the given child has the given PM as parent or is the parent object itself.
   */
  // TODO: rename to something like isChildOrSelf
  public static boolean isChild(PmObject parentCandidate, PmObject childCandidate) {
    PmObject pm = childCandidate;

    while (pm != parentCandidate && pm != null) {
      pm = pm.getPmParent();
    }

    return pm == parentCandidate;
  }

  /**
   * Reports the set of changed PMs within a sub-tree of PMs.
   * <p>
   * Provides usually a set of changed {@link PmAttr}s.
   *
   * @param searchRootPm The root element to start with.
   * @return The set of changed PMs.
   */
  public static List<PmObject> findChangedPms(PmObject searchRootPm) {
    List<PmObject> listOfChangedPms = new ArrayList<PmObject>();
    _findChangedPms(searchRootPm, listOfChangedPms);
    return listOfChangedPms;
  }

  /**
   * Provides the set of all <b>visible</b> commands.
   * <p>
   * Instances of {@link PmCommandSeparator} will automatically
   * appear/disappear, depending on the visibility of the commands
   * before/after/between these separators.
   *
   * @return The list of visible commands and separators.
   */
  public static List<PmCommand> getVisiblePmCommands(PmObject pm) {
    List<PmCommand> commands = getPmChildrenOfType(pm, PmCommand.class);
    return PmCommandSeparator.filterVisibleCommandsAndSeparators(commands);
  }

  /**
   * @param The command set kind to get.
   * @return The command set that should be shown within the given command set kind.
   */
  public static List<PmCommand> getVisiblePmCommands(PmObject pm, PmCommand.CommandSet commandSet) {
    return ((PmObjectBase)pm).getVisiblePmCommands(commandSet);
  }

  /**
   * @param pm The pm to get the child PM from.
   * @param childName
   *          Name of the requested child PM.
   * @return The matching child instance. Or <code>null</code> if there is no matching child with the given name.
   * @throws PmRuntimeException
   *           if the found child has a different type.
   */
  public static <T extends PmObject> T findPmChildOfType(PmObject pm, String childName, Class<T> childClass) {
    PmObject childPm = ((PmObjectBase)pm).findChildPm(childName);
    if (childPm != null && !(childClass.isAssignableFrom(childPm.getClass()))) {
      throw new PmRuntimeException(pm, "The found child PM with name '" + childName +
          "' is not compatible to the expected type '" + childClass.getName() + "'. Found item: " + childPm);
    }
    return childClass.cast(childPm);
  }

  /**
   * Imperative version of {@link #findPmChildOfType(PmObject, String, Class)}.
   *
   * @param pm
   * @param childName
   * @param childClass
   * @return
   */
  public static <T extends PmObject> T getPmChildOfType(PmObject pm, String childName, Class<T> childClass) {
    T childPm = findPmChildOfType(pm, childName, childClass);
    if (childPm == null) {
      throw new PmRuntimeException(pm, "No child PM found for name '" + childName + "'.");
    }
    return childPm;
  }


  public static Object convertStringToValue(PmAttr<?> pmAttr, String s) throws PmConverterException {
    return ((PmAttrBase<?, ?>)pmAttr).stringToValueImpl(s);
  }

  @SuppressWarnings("unchecked")
  public static String convertToString(PmAttr<?> pmAttr, Object v) {
    return ((PmAttrBase<Object, ?>)pmAttr).valueToStringImpl(v);
  }

  /**
   * Provides the a space separated lists of style classes for the given PM.
   *
   * @param viewMap
   *          An optional mapper that generates style classes according to PM
   *          states.
   * @param pm
   *          The PM that provides the style classes (by calling {@link PmObject#getPmStyleClasses()}.
   * @param fixStyleClass
   *          An optional string with fix style definitions to be added to the PM provided style classes.
   * @return All style classes to be applied to the PM.
   */
  public static String getStyleClassesString(PmObject pm, String fixStyleClass) {
    if (pm != null) {
      Set<String> styleClasses = pm.getPmStyleClasses();
      if (! styleClasses.isEmpty()) {
        StringBuilder sb = new StringBuilder(StringUtils.join(styleClasses, " "));
        if (StringUtils.isNotBlank(fixStyleClass)) {
          sb.append(" ").append(fixStyleClass);
        }
        return sb.toString();
      }
    }

    return StringUtils.defaultString(fixStyleClass);
  }

  /**
   * A name that includes the names of all elements and sessions within the
   * complete context hierarchy of the PM.
   * <p>
   * Example: The name attribute of a user presentation model may have the long
   * name 'userSession.userPm.name'.
   *
   * @return The canonical name.
   */
  public static String getAbsoluteName(PmObject pm) {
    return ((PmObjectBase)pm).getPmMetaDataWithoutPmInitCall().getAbsoluteName();
  }

  /**
   * Provides a string for logging and debugging.
   *
   * @return A 'toString' like output.
   */
  public static String getPmLogString(PmObject pm) {
    if (((PmObjectBase)pm).pmInitState == PmInitState.NOT_INITIALIZED) {
      // Prevents initialization calls only because of some log statements.
      return pm.getClass().getName();
    }
    else {
      return pm.getPmConversation().getPmDefaults().getLogStringBuilder().makeName((PmObjectBase)pm);
    }
  }


  public static void setPmContentAspect(PmObject pm, PmAspect aspect, Serializable value) throws PmConverterException {
    ((PmObjectBase)pm).setPmContentAspect(aspect, value);
  }
  public static Serializable getPmContentAspect(PmObject pm, PmAspect aspect) {
    return ((PmObjectBase)pm).getPmContentAspect(aspect);
  }


  // -- internal helper --

  private static boolean _findChangedPms(PmObject searchRootPm, List<PmObject> listOfChangedPms) {
    boolean isChanged = false;

    if (searchRootPm instanceof PmDataInput) {
      isChanged = ((PmDataInput)searchRootPm).isPmValueChanged();
      if (!isChanged) {
        return false;
      }

      boolean foundChangedChild = false;
      for (PmObject child : getPmChildren(searchRootPm)) {
        if (_findChangedPms(child, listOfChangedPms)) {
          foundChangedChild = true;
        }
      }

      if (!foundChangedChild) {
        listOfChangedPms.add(searchRootPm);
      }

      return true;
    }
    else {
      boolean foundChangedChild = false;
      for (PmObject child : getPmChildren(searchRootPm)) {
        if (_findChangedPms(child, listOfChangedPms)) {
          foundChangedChild = true;
        }
      }
      return foundChangedChild;
    }
  }

}
