package org.pm4j.common.expr.parser;

public class ParseException extends RuntimeException {

  private static final long serialVersionUID = 7470962748762236941L;

  public ParseException(ParseCtxt parseCtxt, String msg) {
    super(makeMsg(parseCtxt, msg));
  }

  private static String makeMsg(ParseCtxt ctxt, String msg) {
    StringBuilder sb = new StringBuilder(200);

    sb.append("Unable to parse '").append(ctxt.getText()).append("':\n")
      .append(msg).append("\n")
      .append("Parse position: ").append(ctxt.getPos());

    return sb.toString();
  }
}
