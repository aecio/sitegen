package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.FsWatcher.FileModifiedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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

public class Main {

    private EventBus events;
    private Config config;
    private ConfigPaths paths;
    private Site site;
    private Templates templates;
    
    public static void main(String[] args) throws IOException {
        new Main().execute(args);
    }

    public Main() {
        this.events = new EventBus("sitegen");
        this.events.register(this);
    }
    
    public void execute(String[] args) throws IOException {

        System.out.println("Starting site generator...");

        System.out.println("Initializing configuration...");
        this.config = Config.create();
        this.paths = new ConfigPaths(config);
        this.site = new Site(config.base_url);

        System.out.println("Loading templates...");
        this.templates = Templates.load(paths.templates);
        
        System.out.println("Compiling pages...");
        this.processPages();
        
        System.out.println("Copying static folder...");
        FileUtils.copyDirectory(paths.staticFiles.toFile(), paths.staticFilesOutput.toFile());
        
        System.out.println("Generation finished.");

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
    public void handerFileModified(FileModifiedEvent event) {
        try {
            long start = System.currentTimeMillis();
            if(event.fsPath.equals(paths.staticFiles)) {
                System.out.println("Static folder modified. Copying static folder...");
                FileUtils.copyDirectory(paths.staticFiles.toFile(), paths.staticFilesOutput.toFile());
            } else if(event.fsPath.equals(paths.templates)) {
                System.out.println("Template modified. Reloading templates...");
                this.templates = Templates.load(paths.templates);
            }
            System.out.println("Recompiling pages...");
            processPages();
            System.out.printf("Done (took %d ms).\n\n", System.currentTimeMillis()-start);
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
        
        for(Page page : allPages) {
            
            Context context = new Context(this.site, page, allPages, pages, posts);
            
            String filename = page.permalink;
            if(page.permalink.endsWith("/")) {
                filename += "/index.html";
            }
            Path outputFilePath = Paths.get(paths.pagesOutput.toString(), filename);

            System.out.println("Rendering page at: "+outputFilePath.toString());
            Files.createDirectories(outputFilePath.getParent());
            templates.renderPage(context, page, outputFilePath);

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
    }
    
    static public class Site {
        public Site(String baseUrl) {
            base_url = baseUrl;
        }

        public String base_url;
    }

    public static class ConfigPaths {

        private Path templates;
        private Path posts;
        private Path pages;
        private Path pagesOutput;
        private Path staticFiles;
        private Path staticFilesOutput;

        public ConfigPaths(Config config) {
            this.templates = Paths.get(config.templates_path);
            this.posts = Paths.get(config.posts_path);
            this.pages = Paths.get(config.pages_path);
            this.pagesOutput = Paths.get(config.output_path);
            this.staticFiles = Paths.get(config.static_path);
            this.staticFilesOutput = Paths.get(config.output_path, "static");
        }
    }

}
