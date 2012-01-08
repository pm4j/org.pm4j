package org.pm4j.core.pm.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pm4j.core.exception.PmRuntimeException;
import org.pm4j.core.pm.PmAspect;
import org.pm4j.core.pm.PmConversation;
import org.pm4j.core.pm.PmMessage;
import org.pm4j.core.pm.PmMessage.Severity;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.api.PmExpressionApi;
import org.pm4j.core.pm.impl.PmUtil;
import org.pm4j.core.pm.impl.expr.ThisExpr;

/**
 * Supports serialization and de-serialization of PM content.
 * <p>
 * Configurable {@link PmAspect}s of PMs may be serialized/de-serialized
 * with the help of serializable {@link PmContentContainer} instances.
 * <p>
 * The transferred PMs are identified by name. These names are in fact PM
 * path expressions that may be resolved by the {@link PmConversation} that provides
 * the context of the receiving {@link PmContentSerializer}.
 * <p>
 * Example:
 *   TODO
 * @author olaf boede
 */
public class PmContentSerializer {

  private static Log LOG = LogFactory.getLog(PmContentSerializer.class);
  private PmContentCfg pmContentCfg;

  /**
   * Initializes the {@link PmContentSerializer} with a content configuration.
   *
   * @param pmContentCfg
   */
  public PmContentSerializer(PmContentCfg pmContentCfg) {
    this.pmContentCfg = pmContentCfg;
  }

  public PmContentSerializer() {
    this(new PmContentCfg(PmAspect.VALUE));
  }

  public void serialize(PmObject pmCtxt, OutputStream os) {
    String pmPath = ThisExpr.THIS_KEYWORD;
    Object o = PmExpressionApi.getByExpression(pmCtxt, pmPath);
    if (o instanceof PmObject) {
      PmContentGetVisitor v = new PmContentGetVisitor(pmContentCfg);
      v.contentContainer.setPmPath(pmPath);
      ((PmObject)o).accept(v);
      SerializationUtils.serialize(v.contentContainer, os);
    }
  }

  public void serializeWithPmMessages(PmObject pmCtxt, OutputStream os) {
    serialize(pmCtxt, os);
    for (PmMessage m : pmCtxt.getPmConversation().getPmMessages()) {
      serializePmMessage(m, os);
    }
  }

  public byte[] serialize(PmObject pmCtxt) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    serialize(pmCtxt, bos);
    return bos.toByteArray();
  }

  public byte[] serializeWithPmMessages(PmObject pmCtxt) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    serializeWithPmMessages(pmCtxt, bos);
    return bos.toByteArray();
  }

  public void serialize(OutputStream os, String remotePmPath, PmObject pm) {
    PmContentGetVisitor v = new PmContentGetVisitor(pmContentCfg);
    v.contentContainer.setPmPath(remotePmPath);
    pm.accept(v);
    if (LOG.isDebugEnabled()) {
      LOG.debug("serialized path '" + remotePmPath + "'. Content:\n" + v.contentContainer.toString());
    }

    SerializationUtils.serialize(v.contentContainer, os);
  }

  public byte[] serialize(String remotePmPath, PmObject pm) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    serialize(bos, remotePmPath, pm);
    return bos.toByteArray();
  }

  /**
   * De-serializes a set of PM content objects from the given stream.
   * <p>
   * The following
   * TODO: we need a kind of content resolver...
   *
   * @param is
   */
  public PmObject deserialize(PmObject pmCtxt, InputStream is) {
    try {
      PmObject pm = null;
      if (is.available() > 0) {
        PmContentContainer c = (PmContentContainer)new ObjectInputStream(is).readObject();
        pm = (PmObject)PmExpressionApi.getByExpression(pmCtxt, c.getPmPath());
        new PmContentChangeCommand(pm, c).doIt();
      }
      while (is.available() > 0) {
        deserializePmMessage(pmCtxt, is);
      }

      return pm;
    } catch (Exception e) {
      throw PmRuntimeException.asPmRuntimeException(pmCtxt, e);
    }
    finally {
        try { is.close(); } catch (IOException ex) { /* ignore close exceptions */ }
    }
  }

  public PmObject deserialize(PmObject pmCtxt, byte[] bytes) {
    return deserialize(pmCtxt, new ByteArrayInputStream(bytes));
  }

  private void serializePmMessage(PmMessage msg, OutputStream os) {
    // TODO: path resolution is not yet implemented
    SerializationUtils.serialize(msg.getPm().getPmName(), os);
    SerializationUtils.serialize(msg.getSeverity(), os);
    SerializationUtils.serialize(msg.getMsgKey(), os);
    SerializationUtils.serialize(msg.getMsgArgs(), os);
  }

  private void deserializePmMessage(PmObject pmCtxt, InputStream is) throws IOException, ClassNotFoundException {
    while (is.available() > 0) {
      String pmPath = (String)new ObjectInputStream(is).readObject();
      Severity severity = (Severity)new ObjectInputStream(is).readObject();
      String msgKey = (String)new ObjectInputStream(is).readObject();
      Object[] args = (Object[])new ObjectInputStream(is).readObject();

      // TODO: path resolution is not yet implemented
      PmObject msgPm = PmUtil.findChildPm(pmCtxt, pmPath);
      if (msgPm != null) {
        pmCtxt.getPmConversation().addPmMessage(new PmMessage(msgPm, severity, msgKey, args));
      }
      else {
        LOG.warn(new PmRuntimeException(pmCtxt, "Unable to find PM '" + pmPath + "'").getMessage());
//          throw new PmRuntimeException(pmCtxt, "Unable to find PM '" + pmPath + "'");
      }
    }
  }


}