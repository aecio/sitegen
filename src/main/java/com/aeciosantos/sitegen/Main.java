package com.aeciosantos.sitegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.aeciosantos.sitegen.FsWatcher.FileModifiedEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.SimpleWebServer;

public class Main {

    private EventBus events;
    private Config config;
    private Site site;
    private Path pagesPath;
    private Path templatesPath;
    private Templates templates;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        new Main().execute(args);
    }
    
    public void execute(String[] args) throws IOException {
        
        System.out.println("Starting site generator...");
        this.events = new EventBus("sitegen");
        this.events.register(this);
        
        this.site = new Site("http://localhost:8080");
        
        this.config = new Config();
        this.pagesPath = Paths.get(config.pages_path);
        this.templatesPath = Paths.get(config.templates_path);
        
        this.templates = Templates.load(templatesPath);
        
        this.processPages(config, site, pagesPath, templates);
        
        System.out.println("Copying static folder...");
        FileUtils.copyDirectory(
            Paths.get(config.static_path).toFile(),
            Paths.get(config.output_path, "static").toFile()
        );
        
        System.out.println("Generation finished.");
        
        SimpleWebServer server = new SimpleWebServer("127.0.0.1", 8080, 
                Paths.get(config.output_path).toFile(), true);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Website available at http://127.0.0.1:8080");
        
        new FsWatcher(events, pagesPath).start();
    }

    @Subscribe
    public void handerFileModified(FileModifiedEvent event) {
        try {
            System.out.println("File modified. Reprocessing website...");
            processPages(config, site, pagesPath, templates);
            System.out.println("Done.\n");
        } catch (IOException e) {
            System.err.println("Failed to generated web site for modified files.");
        }
    }

    private void processPages(Config config, Site site, Path pagesPath, Templates templates) throws IOException {
        
        List<Page> pages = Page.loadPages(pagesPath);
        Files.createDirectories(Paths.get(".", config.output_path).getParent());
        for(Page page : pages) {
            Context context = new Context(site, page, pages);
            Path outputFile = Paths.get(config.output_path, page.permalink);
            System.out.println("Rendering page at: "+outputFile.toString());
            Files.createDirectories(outputFile.getParent());
            templates.renderPage(context, page, outputFile);
        }
    }
    
    static public class Site {
        public Site(String baseUrl) {
            base_url = baseUrl;
        }

        public String base_url;
    }

}
