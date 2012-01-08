package org.pm4j.jsf;

import java.util.HashSet;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.api.PmEventApi;
import org.pm4j.core.pm.impl.PmConversationImpl;
import org.pm4j.core.pm.impl.PmUtil;

/**
 * Cleans session messages after the render phase.
 * That's useful for jsf scenarios where messages (infos and/or errors)
 * are rendered (sometimes jsf issues multiple render requests).
 * After rendering these messages are displayed to the user and obsolete...
 * <p>
 * TODOC: message tag relation...
 * TODOC: singleton design...
 *
 * @author olaf boede
 */
public class PmMessageCleanupListener implements PhaseListener {

  private static final long serialVersionUID = 1L;
  private static final Log LOG = LogFactory.getLog(PmMessageCleanupListener.class);

  private static PmMessageCleanupListener instance;

  transient private HashSet<PmConversationImpl> sessionSet = new HashSet<PmConversationImpl>();

  public PmMessageCleanupListener() {
    assert instance == null : "Illegal attempt to create more than one cleanup listener instance.";
    instance = this;
  }

  public static final PmMessageCleanupListener getInstance() {
    return instance;
  }

  public void addSession(PmConversation pmConversation) {
    synchronized(this) {
      sessionSet.add((PmConversationImpl)pmConversation);
    }
  }

  public void afterPhase(PhaseEvent event) {
    if (! sessionSet.isEmpty()) {
      synchronized(this) {
        if (! sessionSet.isEmpty()) {
          for (PmConversationImpl s : sessionSet) {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Cleaning messages of session '" + PmUtil.getPmLogString(s) + "'.");
            }
            PmEventApi.ensureThreadEventSource(this);
            s.clearPmMessages(s, null);
          }
          sessionSet.clear();
        }
      }
    }
  }

  public void beforePhase(PhaseEvent event) {
  }

  public PhaseId getPhaseId() {
    return PhaseId.RENDER_RESPONSE;
  }

}
