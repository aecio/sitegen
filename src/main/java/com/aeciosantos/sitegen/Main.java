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
    private Templates templates;
    private Path pagesPath;
    private Path templatesPath;
    private Path staticPath;
    private Path outputStaticPath;
    
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
        this.staticPath = Paths.get(config.static_path);
        this.outputStaticPath = Paths.get(config.output_path, "static");
        
        System.out.println("Loading templates...");
        this.templates = Templates.load(templatesPath);
        
        System.out.println("Compiling pages...");
        this.processPages(config, site, pagesPath, templates);
        
        System.out.println("Copying static folder...");
        FileUtils.copyDirectory(staticPath.toFile(), outputStaticPath.toFile());
        
        System.out.println("Generation finished.");
        
        SimpleWebServer server = new SimpleWebServer("127.0.0.1", 8080, 
                Paths.get(config.output_path).toFile(), true);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Website available at http://127.0.0.1:8080");
        
        new FsWatcher(events, pagesPath).start();
        new FsWatcher(events, templatesPath).start();
        new FsWatcher(events, staticPath).start();
    }

    @Subscribe
    public void handerFileModified(FileModifiedEvent event) {
        try {
            long start = System.currentTimeMillis();
            if(event.fsPath.equals(staticPath)) {
                System.out.println("Static folder modified. Copying static folder...");
                FileUtils.copyDirectory(staticPath.toFile(), outputStaticPath.toFile());
            } else if(event.fsPath.equals(templatesPath)) {
                System.out.println("Template modified. Reloading templates...");
                this.templates = Templates.load(templatesPath);
            }
            System.out.println("Recompiling pages...");
            processPages(config, site, pagesPath, templates);
            System.out.printf("Done (took %d ms).\n\n", System.currentTimeMillis()-start);
        } catch (IOException e) {
            System.err.println("Failed to generated web site for modified files.");
        }
    }

    private void processPages(Config config, Site site, Path pagesPath, Templates templates) throws IOException {
        List<Page> pages = Page.loadPages(pagesPath);
        Files.createDirectories(Paths.get(".", config.output_path).getParent());
        for(Page page : pages) {
            Context context = new Context(site, page, pages);
            String file = page.permalink;
            if(page.permalink.endsWith("/")) {
                file += "/index.html";
            }
            Path outputFile = Paths.get(config.output_path, file);
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
