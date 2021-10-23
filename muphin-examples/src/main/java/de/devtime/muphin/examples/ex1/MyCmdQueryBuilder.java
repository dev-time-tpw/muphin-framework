package de.devtime.muphin.examples.ex1;

import de.devtime.muphin.core.cmd.DefaultCmdQueryBuilder;

/**
 * Example for a {@link DefaultCmdQueryBuilder}.
 *
 * @author morrigan
 * @since 0.0.1
 */
public class MyCmdQueryBuilder extends DefaultCmdQueryBuilder {

  /**
   * Adds a command to start the windows calculator.
   *
   * @return this query builder
   * @since 0.0.1
   */
  public MyCmdQueryBuilder startCalculator() {
    addCustomCommand("call calc.exe");
    return this;
  }

  /**
   * Start of the program.
   *
   * @param args no args supported
   * @since 0.0.1
   */
  public static void main(String[] args) {
    new DefaultCmdQueryBuilder()
        .changeDirectory("/tmp")
        .addCustomCommand("mkdir test")
        .changeDirectory("test")
        .addCustomCommand("touch test.txt")
        .getCommand();
  }
}
