package com.aeciosantos.sitegen;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Templates {
    
    private static MustacheRenderer mustache = new MustacheRenderer();
    private static MarkdownRenderer markdown = new MarkdownRenderer();
    
    private Map<String, String> templates;

    public Templates(Map<String, String> templates) {
        this.templates = templates;
    }

    public static Templates load(Path templatesPath) throws IOException {
        if(!Files.isDirectory(templatesPath)) {
            System.err.println("Failed to load templates from: "+templatesPath.toString());
            System.exit(1);
        }
        DirectoryStream<Path> path = Files.newDirectoryStream(templatesPath);
        Map<String, String> templates = new HashMap<String, String>();
        for (Path file : path) {
            System.out.println("Loading template file: "+file.toString());
            byte[] template = Files.readAllBytes(file);
            templates.put(file.getFileName().toString(), new String(template));
        }
        return new Templates(templates);
    }

    public boolean canRender(String contentType) {
        return "mustache".equals(contentType);
    }

    public void renderPage(Context context, Page page, Path outputFile) throws IOException {
        
        if("mustache".equals(page.content_type)) {
            page.content = mustache.renderToString(context, page.content);
        } else if("markdown".equals(page.content_type)) {
            page.content = markdown.renderToString(context, page.content);
        } else {
            System.err.println("WARN: Template engine for internal page content not found: "+page.content_type);
        }
        
        String templateType    = getFileExtension(page.template);
        String templateContent = templates.get(page.template);
        if("mustache".equals(templateType)) {
            mustache.renderToFile(context, templateContent, outputFile);
        } else if("markdown".equals(templateType)) {
            markdown.renderToFile(context, templateContent, outputFile);
        } else {
            System.err.println("ERROR: No template engine found for type: "+templateType);
        }
        
    }
    
    private static String getFileExtension(String template) {
        int dotIndex = template.lastIndexOf('.');
        String templateType = template.substring(dotIndex+1, template.length());
        return templateType;
    }
    
}
