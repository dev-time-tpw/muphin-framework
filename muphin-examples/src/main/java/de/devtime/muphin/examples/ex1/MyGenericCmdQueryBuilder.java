package de.devtime.muphin.examples.ex1;

import de.devtime.muphin.core.cmd.GenericCmdQueryBuilder;

public class MyGenericCmdQueryBuilder extends GenericCmdQueryBuilder<MyGenericCmdQueryBuilder> {

  public MyGenericCmdQueryBuilder startMyApp() {
    addCustomCommand("start myApp.exe");
    return getBuilder();
  }

  @Override
  protected MyGenericCmdQueryBuilder getBuilder() {
    return this;
  }

  public static void main(String[] args) {
    String command = new MyGenericCmdQueryBuilder()
        .changeDirectory("myapp")
        .startMyApp()
        .getCommand();
  }
}
