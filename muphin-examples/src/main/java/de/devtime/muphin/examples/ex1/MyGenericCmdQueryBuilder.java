package de.devtime.muphin.examples.ex1;

import de.devtime.muphin.core.cmd.GenericCmdQueryBuilder;

/**
 * Example for a {@link GenericCmdQueryBuilder}.
 *
 * @author morrigan
 * @since 0.0.1
 */
public class MyGenericCmdQueryBuilder extends GenericCmdQueryBuilder<MyGenericCmdQueryBuilder> {

  /**
   * Adds a command to start a 'myApp' windows application.
   *
   * @return this query builder.
   * @since 0.0.1
   */
  public MyGenericCmdQueryBuilder startMyApp() {
    addCustomCommand("start myApp.exe");
    return getBuilder();
  }

  /**
   * Start of the program.
   *
   * @param args no args supported
   * @since 0.0.1
   */
  public static void main(String[] args) {
    new MyGenericCmdQueryBuilder()
        .changeDirectory("myapp")
        .startMyApp()
        .getCommand();
  }

  @Override
  protected MyGenericCmdQueryBuilder getBuilder() {
    return this;
  }
}
