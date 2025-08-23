package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.FileWatcherService.FileModifiedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
    name = "sitegen",
    description = "A simple static site generator that works.",
    subcommands = {
      Main.Build.class,
      Main.Watch.class,
      Main.SitegenHelp.class,
      Main.SitegenVersion.class,
    },
    versionProvider = Main.SitegenVersion.class)
public class Main {

  public static void main(String[] args) {
    System.exit(new CommandLine(new Main()).execute(args));
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
      CommandLine.usage(new Main(), System.out);
    }
  }

  @Command(name = "version", description = "Shows version information.")
  public static class SitegenVersion implements Runnable, CommandLine.IVersionProvider {
    @Override
    public void run() {
      System.out.println("Sitegen version " + getVersion()[0]);
    }

    @Override
    public String[] getVersion() {
      return new String[] {Main.class.getPackage().getImplementationVersion()};
    }
  }

  private static void printErrorAndExit(Exception e, String s) {
    System.out.printf(s + "\n\n");
    e.printStackTrace(System.out);
    System.exit(1);
  }
}
