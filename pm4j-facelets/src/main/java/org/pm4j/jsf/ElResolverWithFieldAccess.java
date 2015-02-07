package org.pm4j.jsf;

import org.apache.commons.lang.ObjectUtils;

import javax.el.BeanELResolver;
import javax.el.ELContext;
import javax.el.PropertyNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An EL property resolver that allows to access field values. We have currently 2000 PMs / 0.75
 * loadFactor approx. 3000 initial capacity.
 *
 * @author Olaf Boede
 * @author Michael Friedrich
 */
public class ElResolverWithFieldAccess extends BeanELResolver {

    /**
     * A cache that prevents repeated reflection based searches for the same field.
     */
    private Map<Class<?>, Map<Object, Field>> classToPropToAccessorMap = new ConcurrentHashMap<Class<?>, Map<Object, Field>>(3000);

    /**
     * Defines if only public final fields can be accessed.
     */
    private boolean accessOnlyFinalFields = false;

    /**
     * Enhances the inherited implementation to allow to access public fields.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null) {
            // 0. Null cases are handled by the base implementation.

            // XXX HGN: Although
            // the current implementation of JSF returns null in this case a java
            // doc says something different so we should not rely of the current
            // implementation which might change any time.
            return super.getValue(context, base, property);

        } else if (notResolvable(base)) {
            context.setPropertyResolved(false);
            return null;
        }

        // 1. Try to find and use a cached Field.
        Map<Object, Field> propToAccessorMap = classToPropToAccessorMap.get(base.getClass());

        if (propToAccessorMap == null) {
            propToAccessorMap = new ConcurrentHashMap<Object, Field>();
            classToPropToAccessorMap.put(base.getClass(), propToAccessorMap);
        } else {
            Field f = propToAccessorMap.get(property);
            if (f != null) {
                Object value = getFieldValue(base, f);
                context.setPropertyResolved(true);
                return value;
            }
        }

        // 2. If there was no cached field: Use the default (getter-based) default
        // implementation.
        try {
            return super.getValue(context, base, property);
        } catch (RuntimeException getterAccessException) {
            // 3. If there was no corresponding getter: Find and use a corresponding field.
            return findField(context, base, property, propToAccessorMap, getterAccessException);
        }
    }

    /**
     * Our custom resolver is somehow ordered in front of other resource handlers and must
     * therefore return gracefully in some cases
     *
     */
    protected boolean notResolvable(Object base) {
        // MMA, 02.08.2012: Our custom resolver is somehow ordered in front of the
        // ResourceBundleELResolver which leads to PNFE if base is a ResourceBundle (e.g.
        // when using dualList)
        boolean resourceBundle = base instanceof ResourceBundle;
        // Let the another non-bean resolver handle collection element references
        boolean collection = Collection.class.isAssignableFrom(base.getClass());
        return resourceBundle || collection;
    }

    private Object findField(ELContext context, Object base, Object property, Map<Object, Field> propToAccessorMap, RuntimeException getterAccessException) {
        try {
            String name = ObjectUtils.toString(property);
            Field f = base.getClass().getField(name);

            if (accessOnlyFinalFields && !Modifier.isFinal(f.getModifiers())) {
                throw new PropertyNotFoundException("Only 'public final' fields can be accessed by EL expressions. Issue found in field '" + name + "' of class " + base.getClass().getName());
            }

            Object result = getFieldValue(base, f);

            // ... cache the found field and return the result.
            propToAccessorMap.put(property, f);

            context.setPropertyResolved(true);
            return result;
        } catch (PropertyNotFoundException e) {
            throw e;
        } catch (Exception e2) {
            throw getterAccessException;
        }
    }

    /**
     * Allows to configure that only final fields area accepted.
     *
     * @param accessOnlyFinalFields
     */
    public void setAccessOnlyFinalFields(boolean accessOnlyFinalFields) {
        this.accessOnlyFinalFields = accessOnlyFinalFields;
    }

    private static final Object getFieldValue(Object o, Field f) {
        try {
            return f.get(o);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
