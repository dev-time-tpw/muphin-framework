package de.devtime.muphin.examples.ex1;

import de.devtime.muphin.core.cmd.DefaultCmdQueryBuilder;

public class MyCmdQueryBuilder extends DefaultCmdQueryBuilder {

  public MyCmdQueryBuilder startCalculator() {
    addCustomCommand("call calc.exe");
    return this;
  }

  public static void main(String[] args) {
    new DefaultCmdQueryBuilder()
        .changeDirectory("/tmp")
        .addCustomCommand("mkdir test")
        .changeDirectory("test")
        .addCustomCommand("touch test.txt")
        .getCommand();
  }

}
