package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.FsWatcher.FileModifiedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import fi.iki.elonen.NanoHTTPD;
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
    private Site site;
    private Templates templates;
    private Path pagesPath;
    private Path templatesPath;
    private Path staticPath;
    private Path outputStaticPath;
    private Path pagesOutputPath;
    private Path postsPath;
    
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

        this.site = new Site(config.base_url);

        this.pagesPath = Paths.get(config.pages_path);
        this.postsPath = Paths.get(config.posts_path);
        this.templatesPath = Paths.get(config.templates_path);
        this.staticPath = Paths.get(config.static_path);
        this.pagesOutputPath = Paths.get(config.output_path);
        this.outputStaticPath = Paths.get(config.output_path, "static");
        
        System.out.println("Loading templates...");
        this.templates = Templates.load(templatesPath);
        
        System.out.println("Compiling pages...");
        this.processPages();
        
        System.out.println("Copying static folder...");
        FileUtils.copyDirectory(staticPath.toFile(), outputStaticPath.toFile());
        
        System.out.println("Generation finished.");

        int port = config.server_port;
        String host = config.server_host;

        File serverPath = Paths.get(config.output_path).toFile();
        SimpleWebServer server = new SimpleWebServer(host, port, serverPath, true);
        server.start(0, false);
        System.out.println("Website available at http://" + host + ":" + port);
        
        new FsWatcher(events, pagesPath).start();
        new FsWatcher(events, postsPath).start();
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
            processPages();
            System.out.printf("Done (took %d ms).\n\n", System.currentTimeMillis()-start);
        } catch (IOException e) {
            System.err.println("Failed to generated web site for modified files.");
        }
    }

    private void processPages() throws IOException {
        
        List<Page> pages = Page.loadPages(pagesPath);
        List<Page> posts = Page.loadPages(postsPath);
        pages.sort(Page.DESC);
        posts.sort(Page.DESC);
        
        List<Page> allPages = new ArrayList<>();
        allPages.addAll(pages);
        allPages.addAll(posts);
        allPages.sort(Page.DESC);
        
        for(Page page : allPages) {
            
            Context context = new Context(site, page, allPages, pages, posts);
            
            String filename = page.permalink;
            if(page.permalink.endsWith("/")) {
                filename += "/index.html";
            }
            Path outputFilePath = Paths.get(pagesOutputPath.toString(), filename);
            
            System.out.println("Rendering page at: "+outputFilePath.toString());
            Files.createDirectories(outputFilePath.getParent());
            templates.renderPage(context, page, outputFilePath);


            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            System.out.println(mapper.writeValueAsString(page));
        }
    }
    
    static public class Site {
        public Site(String baseUrl) {
            base_url = baseUrl;
        }

        public String base_url;
    }

}
