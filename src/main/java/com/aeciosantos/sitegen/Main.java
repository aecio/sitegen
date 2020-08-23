package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.Config.ConfigPaths;
import com.aeciosantos.sitegen.FsWatcher.FileModifiedEvent;
import com.aeciosantos.sitegen.util.CliTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.rvesse.airline.annotations.Command;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fi.iki.elonen.SimpleWebServer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

@Command(name = "sitegen", description = "A minimal static site generator that works.")
public class Main extends CliTool {

  public static final String VERSION = Main.class.getPackage().getImplementationVersion();

  private EventBus events;
  private Config config;
  private ConfigPaths paths;
  private Site site;
  private Templates templates;

  public static void main(String[] args) {
    printVersion();
    CliTool.run(args, Main.class);
  }

  public Main() {
    this.events = new EventBus("sitegen");
    this.events.register(this);
  }

  @Override
  public void execute() throws Exception {

    System.out.println("Starting site generator...");

    System.out.println("Initializing configuration...");
    this.config = Config.create();
    this.paths = config.getPaths();
    this.site = new Site(config.base_url);

    System.out.println("Loading templates...");
    this.templates = Templates.load(paths.templates);

    System.out.println("Compiling pages...");
    this.processPages();

    System.out.println("Copying static folder...");
    FileUtils.copyDirectory(paths.staticFiles.toFile(), paths.staticFilesOutput.toFile());

    System.out.println("Generation finished.");

    startServer();
  }

  private void startServer() throws IOException {
    int port = config.server_port;
    String host = config.server_host;
    File serverPath = Paths.get(config.output_path).toFile();

    SimpleWebServer server = new SimpleWebServer(host, port, serverPath, true);
    server.start(0, false);

    System.out.println("Website available at http://" + host + ":" + port);

    new FsWatcher(events, paths.pages).start();
    new FsWatcher(events, paths.posts).start();
    new FsWatcher(events, paths.templates).start();
    new FsWatcher(events, paths.staticFiles).start();
  }

  @Subscribe
  public void handleFileModified(FileModifiedEvent event) {
    try {
      long start = System.currentTimeMillis();
      if (event.fsPath.equals(paths.staticFiles)) {
        System.out.println("Static folder modified. Copying static folder...");
        FileUtils.copyDirectory(paths.staticFiles.toFile(), paths.staticFilesOutput.toFile());
      } else if (event.fsPath.equals(paths.templates)) {
        System.out.println("Template modified. Reloading templates...");
        this.templates = Templates.load(paths.templates);
      }
      System.out.println("Recompiling pages...");
      processPages();
      System.out.printf("Done (took %d ms).\n\n", System.currentTimeMillis() - start);
    } catch (IOException e) {
      System.err.println("Failed to generated web site for modified files.");
    }
  }

  private void processPages() throws IOException {

    List<Page> pages = Page.loadPages(paths.pages);
    List<Page> posts = Page.loadPages(paths.posts);
    pages.sort(Page.DESC);
    posts.sort(Page.DESC);

    List<Page> allPages = new ArrayList<>();
    allPages.addAll(pages);
    allPages.addAll(posts);
    allPages.sort(Page.DESC);

    for (Page page : allPages) {

      Context context = new Context(this.site, page, allPages, pages, posts);

      String filename = page.permalink;
      if (page.permalink.endsWith("/")) {
        filename += "/index.html";
      }
      Path outputFilePath = Paths.get(paths.pagesOutput.toString(), filename);

      System.out.println("Rendering page at: " + outputFilePath.toString());
      Files.createDirectories(outputFilePath.getParent());
      templates.renderPage(context, page, outputFilePath);

      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
  }

  public static class Site {
    public Site(String baseUrl) {
      base_url = baseUrl;
    }

    public String base_url;
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
