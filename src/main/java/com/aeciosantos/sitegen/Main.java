package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.FileWatcherService.FileModifiedEvent;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.util.Arrays;

public class Main {

  public static final String VERSION = Main.class.getPackage().getImplementationVersion();

  public static void main(String[] args) throws IOException {
    printVersion();
    Cli<Runnable> cli = createCli();
    try {
      cli.parse(args).run();
    } catch (ParseException e) {
      System.out.println("Unable to parse the input. " + e.getMessage() + "\n");
      printHelp(cli);
      System.exit(1);
    } catch (Exception e) {
      System.err.println("Failed to execute command.");
      e.printStackTrace(System.err);
    }
  }

  private static Cli<Runnable> createCli() {
    return Cli.<Runnable>builder("sitegen")
        .withDescription("sitegen - A simple static site generator that works.")
        .withDefaultCommand(SitegenHelp.class)
        .withCommands(Build.class, Watch.class, SitegenHelp.class)
        .build();
  }

  @Command(name = "build", description = "Compile the source files into static web pages.")
  public static class Build implements Runnable {

    @Override
    public void run() {
      try {
        buildSiteFromSource();
      } catch (Exception e) {
        printErrorAndExit(e, "Failed to build site.");
      }
    }

    public void buildSiteFromSource() throws Exception {
      Config config = Config.create();
      SiteGenerator generator = new SiteGenerator(config);
      generator.generate();
    }
  }

  @Command(name = "watch", description = "Starts a web server and watches for source file changes.")
  public static class Watch implements Runnable {

    private SiteGenerator generator;

    @Override
    public void run() {
      try {
        execute();
      } catch (Exception e) {
        printErrorAndExit(e, "Failed to compile site and start watcher.");
      }
    }

    /*
     * Compiles source code to static web pages, starts a embedded web server and watches for source
     * file changes.
     */
    public void execute() throws Exception {

      Config config = Config.create();

      generator = new SiteGenerator(config);
      generator.generate();

      EventBus eventBus;
      eventBus = new EventBus("sitegen");
      eventBus.register(this);

      FileWatcherService watcher = new FileWatcherService(eventBus, config);
      watcher.start();

      WebServer server = new WebServer(config);
      server.startWebServer();
    }

    @Subscribe
    public void handleFileModified(FileModifiedEvent event) {
      generator.handleFileModified(event);
    }
  }

  @Command(name = "help", description = "Shows help information.")
  public static class SitegenHelp implements Runnable {

    @Override
    public void run() {
      try {
        printHelp(createCli());
      } catch (Exception e) {
        printErrorAndExit(e, "Failed to show help message.");
      }
    }
  }

  private static void printHelp(Cli<Runnable> cli) throws IOException {
    Help.help(cli.getMetadata(), Arrays.asList());
  }

  private static void printErrorAndExit(Exception e, String s) {
    System.out.printf(s + "\n\n");
    e.printStackTrace(System.out);
    System.exit(1);
  }

  private static void printVersion() {
    String header = "sitegen " + VERSION;
    for (int i = 0; i < header.length(); i++) {
      System.out.print("-");
    }
    System.out.println();
    System.out.println(header);
    for (int i = 0; i < header.length(); i++) {
      System.out.print("-");
    }
    System.out.println();
    System.out.println();
  }
}
