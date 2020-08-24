package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.Config.ConfigPaths;
import com.aeciosantos.sitegen.FileWatcherService.FileModifiedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class SiteGenerator {

  private final Site site;
  private final ConfigPaths paths;
  private Templates templates;

  public SiteGenerator(Config config) {
    this.paths = config.getPaths();
    this.site = new Site(config.base_url);
    this.templates = Templates.load(paths.templates);
  }

  public void generate() throws IOException {
    System.out.println("Compiling pages...");
    processPages();

    System.out.println("Copying static folder...");
    FileUtils.copyDirectory(paths.staticFiles.toFile(), paths.staticFilesOutput.toFile());

    System.out.println("Compilation finished.");
  }

  public void processPages() throws IOException {
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

  public static class Site {

    public Site(String baseUrl) {
      base_url = baseUrl;
    }

    public String base_url;
  }
}
