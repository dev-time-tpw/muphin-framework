package de.devtime.test.muphin.task;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.logging.log4j.core.util.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.devtime.muphin.core.cmd.CmdResponse;
import de.devtime.muphin.core.exception.MuphinFailureException;
import de.devtime.muphin.core.task.CmdTask;
import de.devtime.muphin.core.task.Task.Verification;

@RunWith(MockitoJUnitRunner.class)
public class CmdTaskTest {

  private class CmdTaskMock extends CmdTask {

    @SafeVarargs
    public CmdTaskMock(String executor, String command, Verification<CmdResponse>... verifications) {
      super(executor, command, verifications);
    }

    @SafeVarargs
    public CmdTaskMock(String executor, String command, long timeout, Verification<CmdResponse>... verifications) {
      super(executor, command, timeout, verifications);
    }

    public CmdTaskMock(String executor, String command) {
      super(executor, command);
    }

    @Override
    protected DefaultExecutor getExecutor() {
      return CmdTaskTest.this.executorMock;
    }

    @Override
    protected DefaultExecuteResultHandler getExecuteResultHandler() {
      return CmdTaskTest.this.handlerMock;
    }
  }

  @Mock
  private DefaultExecuteResultHandler handlerMock;

  @Mock
  private DefaultExecutor executorMock;

  @Before
  public void setup() throws ExecuteException, IOException {
    when(this.handlerMock.hasResult()).thenReturn(Boolean.TRUE);
    doNothing().when(this.executorMock).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testSuccessExitVerificationWithExitValueEqualToZero() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_EXIT;
    boolean result = sut.verify(new CmdResponse(0, null, null));
    assertTrue(result);
  }

  @Test
  public void testSuccessExitVerificationWithExitValueEqualToOne() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_EXIT;
    boolean result = sut.verify(new CmdResponse(1, null, null));
    assertFalse(result);
  }

  @Test
  public void testSuccessAllVerificationWithExitValueEqualToZero() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_ALL;
    boolean result = sut.verify(new CmdResponse(0, null, null));
    assertTrue(result);
  }

  @Test
  public void testSuccessAllVerificationWithExitValueEqualToOne() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_ALL;
    boolean result = sut.verify(new CmdResponse(1, null, null));
    assertFalse(result);
  }

  @Test
  public void testSuccessAllVerificationWithMessageValueSet() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_ALL;
    boolean result = sut.verify(new CmdResponse(0, "Error", null));
    assertFalse(result);
  }

  @Test
  public void testSuccessAllVerificationWithMessageValueEqualToBlank() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_ALL;
    boolean result = sut.verify(new CmdResponse(0, "", null));
    assertTrue(result);
  }

  @Test
  public void testSuccessAllVerificationWithExceptionValueSet() {
    Verification<CmdResponse> sut = CmdTask.SUCCESS_ALL;
    boolean result = sut.verify(new CmdResponse(0, null, new NullPointerException()));
    assertFalse(result);
  }

  @Test
  public void testVerifySuccessWithExitValueEqualToZeroWithoutMsgs() {
    boolean result = CmdTask.verifySuccess(new CmdResponse(0, null, null));
    assertTrue(result);
  }

  @Test
  public void testVerifySuccessWithExitValueEqualToZeroWithMsg() {
    boolean result = CmdTask.verifySuccess(new CmdResponse(0, "Hello World!", null), "Hello");
    assertTrue(result);
  }

  @Test
  public void testVerifySuccessWithExitValueEqualToZeroWithNotContainingMsg() {
    boolean result = CmdTask.verifySuccess(new CmdResponse(0, "Hello World!", null), "Tom");
    assertFalse(result);
  }

  @Test
  public void testVerifySuccessWithExitValueEqualToOneWithoutMsgs() {
    boolean result = CmdTask.verifySuccess(new CmdResponse(1, "Hello World!", null));
    assertFalse(result);
  }

  @Test
  public void testVerifySuccessWithExitValueEqualToOneWithNotContainingMsg() {
    boolean result = CmdTask.verifySuccess(new CmdResponse(1, "Hello World!", null), "Tom");
    assertFalse(result);
  }

  @Test
  public void testVerifyMessageContainsAnyReturnsTrue() {
    boolean result = CmdTask.verifyMessageContainsAny(new CmdResponse(0, "Hello World!", null), "Hello", "Tom");
    assertTrue(result);
  }

  @Test
  public void testVerifyMessageContainsAnyReturnsFalse() {
    boolean result = CmdTask.verifyMessageContainsAny(new CmdResponse(0, "Hello World!", null), "Tom");
    assertFalse(result);
  }

  @Test
  public void testVerifyMessageContainsAllReturnsTrue() {
    boolean result = CmdTask.verifyMessageContainsAll(new CmdResponse(0, "Hello World!", null), "Hello", "World");
    assertTrue(result);
  }

  @Test
  public void testVerifyMessageContainsAllReturnsTrueWithoutMegs() {
    boolean result = CmdTask.verifyMessageContainsAll(new CmdResponse(0, "Hello World!", null));
    assertTrue(result);
  }

  @Test
  public void testVerifyMessageContainsAllReturnsFalse() {
    boolean result = CmdTask.verifyMessageContainsAll(new CmdResponse(0, "Hello World!", null), "Hello", "Tom");
    assertFalse(result);
  }

  @Test
  public void testVerifyMessageNotContainsReturnsTrue() {
    boolean result = CmdTask.verifyMessageNotContains(new CmdResponse(0, "Hello World!", null), "Tom");
    assertTrue(result);
  }

  @Test
  public void testVerifyMessageNotContainsReturnsFalse() {
    boolean result = CmdTask.verifyMessageNotContains(new CmdResponse(0, "Hello World!", null), "Hello");
    assertFalse(result);
  }

  @Test
  public void testVerifyMessageNotContainsReturnsFalseWithoutMsgs() {
    boolean result = CmdTask.verifyMessageNotContains(new CmdResponse(0, "Hello World!", null));
    assertFalse(result);
  }

  @Test
  public void testConstructionWithoutVerification() {
    CmdTask task = new CmdTask("cmd /c", "dir", 1000);
    assertThat(getCommand(task), is(equalTo("cmd /c dir")));
    assertThat(getVerifications(task).length, is(equalTo(0)));
    assertThat(getAsynchron(task), is(equalTo(false)));
  }

  @Test
  public void testConstructionWithVerification() {
    CmdTask task = new CmdTask("cmd /c", "dir", 1000,
        responseData -> false,
        responseData -> true);
    assertThat(getCommand(task), is(equalTo("cmd /c dir")));
    assertThat(getVerifications(task).length, is(equalTo(2)));
    assertThat(getAsynchron(task), is(equalTo(false)));
  }

  @Test
  public void testConstructionWithAsynchron() {
    CmdTask task = new CmdTask("cmd /c", "dir");
    assertThat(getCommand(task), is(equalTo("cmd /c dir")));
    assertThat(getVerifications(task).length, is(equalTo(1)));
    assertThat(getAsynchron(task), is(equalTo(true)));
  }

  @Test
  public void testExecuteWithoutValidations() throws MuphinFailureException, ExecuteException, IOException {
    CmdTask task = new CmdTaskMock("cmd /c", "dir", 1000);
    task.execute();

    verify(this.handlerMock, atLeastOnce()).hasResult();
    verify(this.executorMock, times(1)).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testExecuteWithSuccessValidation()
      throws MuphinFailureException, ExecuteException, IOException, InterruptedException {
    doNothing().when(this.handlerMock).waitFor(Mockito.anyLong());
    when(this.handlerMock.hasResult()).thenReturn(Boolean.FALSE, Boolean.TRUE);
    CmdTask task = new CmdTaskMock("cmd /c", "dir", responseData -> true);
    task.execute();

    verify(this.handlerMock, atLeastOnce()).hasResult();
    verify(this.handlerMock, atLeastOnce()).waitFor(Mockito.anyLong());
    verify(this.executorMock, times(1)).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testExecuteWithDelayedResponseInSynchronyMode()
      throws MuphinFailureException, ExecuteException, IOException, InterruptedException {
    when(this.handlerMock.hasResult()).thenReturn(Boolean.FALSE, Boolean.TRUE);
    doAnswer(invocation -> {
      await().atLeast(100, TimeUnit.MICROSECONDS);
      return null;
    }).when(this.executorMock).execute(Mockito.any(CommandLine.class), Mockito.any(ExecuteResultHandler.class));

    CmdTask task = new CmdTaskMock("cmd /c", "dir", responseData -> true);
    task.execute();

    verify(this.handlerMock, atLeastOnce()).hasResult();
    verify(this.handlerMock, atLeastOnce()).waitFor(Mockito.anyLong());
    verify(this.executorMock, times(1)).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testExecuteWithDelayedResponseInAsynchronyMode()
      throws MuphinFailureException, ExecuteException, IOException, InterruptedException {
    when(this.handlerMock.hasResult()).thenReturn(Boolean.FALSE);
    doAnswer(invocation -> {
      await().atLeast(100, TimeUnit.MICROSECONDS);
      return null;
    }).when(this.executorMock).execute(Mockito.any(CommandLine.class), Mockito.any(ExecuteResultHandler.class));

    CmdTask task = new CmdTaskMock("cmd /c", "dir");
    task.execute();

    verify(this.handlerMock, atLeastOnce()).hasResult();
    verify(this.handlerMock, never()).waitFor(Mockito.anyLong());
    verify(this.executorMock, times(1)).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testExecuteWithTimeout()
      throws MuphinFailureException, ExecuteException, IOException, InterruptedException {
    when(this.handlerMock.hasResult()).thenReturn(Boolean.FALSE);
    doNothing().when(this.handlerMock).waitFor(Mockito.anyLong());
    doNothing().when(this.executorMock).execute(Mockito.any(CommandLine.class),
        Mockito.any(ExecuteResultHandler.class));

    CmdTask task = new CmdTaskMock("cmd /c", "dir", 50, responseData -> true);
    MuphinFailureException exception = assertThrows(MuphinFailureException.class, () -> task.execute());
    assertThat(exception.getMessage(), containsString("cmd /c dir"));
    assertThat(exception.getMessage(), containsString("does not finish execution within 0.05 seconds"));
  }

  @Test
  public void testExecuteWithIOException() throws MuphinFailureException, ExecuteException, IOException {
    IOException ioe = new IOException("TestMsg");
    doThrow(ioe).when(this.executorMock).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));

    CmdTask task = new CmdTaskMock("cmd /c", "dir", CmdTask.SUCCESS_ALL);
    MuphinFailureException exception = assertThrows(MuphinFailureException.class, () -> task.execute());
    assertThat(exception.getMessage(), containsString("TestMsg"));
    assertThat(exception.getCause(), is(equalTo(ioe)));

    verify(this.handlerMock, atLeastOnce()).hasResult();
    verify(this.executorMock, times(1)).execute(Mockito.any(CommandLine.class), Mockito.eq(this.handlerMock));
  }

  @Test
  public void testExecuteWithInterruptedException() throws MuphinFailureException, InterruptedException {
    InterruptedException ie = new InterruptedException("TestMsg");
    when(this.handlerMock.hasResult()).thenReturn(Boolean.FALSE, Boolean.TRUE);
    doThrow(ie).when(this.handlerMock).waitFor(Mockito.anyLong());

    CmdTask task = new CmdTaskMock("cmd /c", "dir", CmdTask.SUCCESS_ALL);
    MuphinFailureException exception = assertThrows(MuphinFailureException.class, () -> task.execute());
    assertThat(exception.getMessage(), containsString("TestMsg"));
    assertThat(exception.getCause(), is(equalTo(ie)));
  }

  @Test
  public void testExecuteWithFailingValidation() throws MuphinFailureException {
    CmdTask task = new CmdTaskMock("cmd /c", "dir", responseData -> false);
    MuphinFailureException exception = assertThrows(MuphinFailureException.class, () -> task.execute());
    assertThat(exception.getMessage(), containsString("cmd /c dir"));
    assertThat(exception.getMessage(), containsString("was not executed successfully"));
    assertThat(exception.getMessage(), containsString("exit value 0"));
  }

  private String getCommand(CmdTask sut) {
    try {
      return (String) ReflectionUtil.getFieldValue(CmdTask.class.getDeclaredField("command"), sut);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private Verification<CmdResponse>[] getVerifications(CmdTask sut) {
    try {
      return (Verification<CmdResponse>[]) ReflectionUtil.getFieldValue(
          CmdTask.class.getDeclaredField("verifications"), sut);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }

  private boolean getAsynchron(CmdTask sut) {
    try {
      return (boolean) ReflectionUtil.getFieldValue(CmdTask.class.getDeclaredField("asynchron"), sut);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }

}
