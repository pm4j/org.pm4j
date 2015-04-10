package org.pm4j.core.pm;

/**
 * Test helper that records the number of decorator calls.
 * 
 * @author Olaf Boede
 */
public class RecordingCommandDecorator implements PmCommandDecorator.WithExceptionHandling {
  private int beforeCallCount = 0;
  private int afterCallCount = 0;
  private int exceptionCallCount = 0;

  @Override
  public boolean beforeDo(PmCommand cmd) {
    ++beforeCallCount;
    return true;
  }

  @Override
  public void afterDo(PmCommand cmd) {
    ++afterCallCount;
  }

  @Override
  public boolean onException(PmCommand cmd, Exception exception) {
    ++exceptionCallCount;
    return true;
  }

  public int getBeforeCallCount() {
    return beforeCallCount;
  }

  public int getAfterCallCount() {
    return afterCallCount;
  }

  public int getExceptionCallCount() {
    return exceptionCallCount;
  }
}