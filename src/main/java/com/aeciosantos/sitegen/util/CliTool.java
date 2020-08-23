package com.aeciosantos.sitegen.util;

import com.github.rvesse.airline.HelpOption;
import com.github.rvesse.airline.SingleCommand;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.model.MetadataLoader;
import com.github.rvesse.airline.parser.errors.ParseException;
import java.io.IOException;

public abstract class CliTool extends HelpOption implements Runnable {

  public void run() {
    try {
      execute();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public abstract void execute() throws Exception;

  public static void run(String[] args, CliTool tool) {
    try {
      SingleCommand<? extends CliTool> cmd = SingleCommand.singleCommand(tool.getClass());
      CliTool cli = cmd.parse(args);
      if (cli.showHelpIfRequested()) {
        return;
      }
      cli.execute();
    } catch (ParseException e) {
      System.out.println("Unable to parse the input. " + e.getMessage() + "\n\n");
      try {
        Help.help(MetadataLoader.loadCommand(tool.getClass()));
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      System.exit(1);
    } catch (Exception e) {
      System.out.println("Failed to execute command.");
      e.printStackTrace(System.out);
      System.exit(1);
    }
  }
}
