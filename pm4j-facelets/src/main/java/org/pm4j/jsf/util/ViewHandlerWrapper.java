package org.pm4j.jsf.util;

import java.io.IOException;
import java.util.Locale;

import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 * A replacement of the JSF 1.2 view handler wrapper.
 * (see <a href="http://java.sun.com/j2ee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>).
 * <p>
 * Was introduced to stay compatible to JSF 1.1.
 *
 * @author Stan Silvert
 */
public abstract class ViewHandlerWrapper extends ViewHandler {

   @Override
   public String calculateCharacterEncoding(FacesContext context) {
       return getWrapped().calculateCharacterEncoding(context);
   }

   @Override
   public void initView(FacesContext context) throws FacesException {
       getWrapped().initView(context);
   }

   protected abstract ViewHandler getWrapped();

   public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
       getWrapped().renderView(context, viewToRender);
   }

   public void writeState(FacesContext context) throws IOException {
       getWrapped().writeState(context);
   }

   public String calculateRenderKitId(FacesContext context) {
       return getWrapped().calculateRenderKitId(context);
   }

   public Locale calculateLocale(FacesContext context) {
       return getWrapped().calculateLocale(context);
   }

   public UIViewRoot restoreView(FacesContext context, String viewId) {
       return getWrapped().restoreView(context, viewId);
   }

   public String getResourceURL(FacesContext context, String path) {
       return getWrapped().getResourceURL(context, path);
   }

   public String getActionURL(FacesContext context, String viewId) {
       return getWrapped().getActionURL(context, viewId);
   }

   public UIViewRoot createView(FacesContext context, String viewId) {
       return getWrapped().createView(context, viewId);
   }

}

