package de.devtime.muphin.core.cmd;

/**
 * A response container that contains various return information from a command line call.
 *
 * @author morrigan
 * @since 0.0.1
 */
public class CmdResponse {

  private int exitValue;
  private String message;
  private Exception exception;

  /**
   * Create a new instance from this response container and sets the given information.
   *
   * @param exitValue an exit value from the command line execution
   * @param message a message that comes from the command line output
   * @param exception an exception, if one occurred while executing the command line task
   * @since 0.0.1
   */
  public CmdResponse(int exitValue, String message, Exception exception) {
    super();
    this.exitValue = exitValue;
    this.message = message;
    this.exception = exception;
  }

  /**
   * Returns an exit value from the command line execution.
   *
   * @return an exit value
   * @since 0.0.1
   */
  public int getExitValue() {
    return this.exitValue;
  }

  /**
   * Returns a message that comes from the command line output.
   *
   * @return a message
   * @since 0.0.1
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Returns an exception, if one occurred while executing the command line task.
   *
   * @return an exception
   * @since 0.0.1
   */
  public Exception getException() {
    return this.exception;
  }
}
