package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.renderer.FreemakerRenderer;
import com.aeciosantos.sitegen.renderer.MarkdownRenderer;
import com.aeciosantos.sitegen.renderer.MustacheRenderer;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Templates {
    
    private static MustacheRenderer mustache = new MustacheRenderer();
    private static MarkdownRenderer markdown = new MarkdownRenderer();
    private static FreemakerRenderer freemaker = new FreemakerRenderer();

    private Map<String, String> templates;

    public Templates(Map<String, String> templates) {
        this.templates = templates;
    }

    public static Templates load(Path templatesPath) throws IOException {
        if(!Files.isDirectory(templatesPath)) {
            System.err.println("Failed to load templates from: "+templatesPath.toString());
            System.err.println("Reason: " + templatesPath.toString() + " is not a directory.");
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

    public void renderPage(Context context, Page page, Path outputFile) throws IOException {

        String contentType = page.content_type == null ? "" : page.content_type;
        switch (contentType.toLowerCase()) {
            case "mustache":
                page.content = mustache.renderToString(context, page.content);
                break;
            case "freemaker":
                page.content = freemaker.renderToString(context, page.content);
                break;
            case "markdown":
                page.content = markdown.renderToString(context, page.content);
                page.content = mustache.renderToString(context, page.content); // substitute variables even if using markdown template
                break;
            default:
                System.err.println("WARN: Template engine for internal page content not found: " + page.content_type);
        }

        String templateType = getFileExtension(page.template);
        String templateContent = templates.get(page.template);
        switch (templateType.toLowerCase()) {
            case "mustache":
                mustache.renderToFile(context, templateContent, outputFile);
                break;
            case "freemaker":
                freemaker.renderToFile(context, templateContent, outputFile);
                break;
            case "markdown":
                markdown.renderToFile(context, templateContent, outputFile);
                break;
            default:
                System.err.println("ERROR: No template engine found for type: " + templateType);
        }
    }
    
    private static String getFileExtension(String template) {
        int dotIndex = template.lastIndexOf('.');
        String templateType = template.substring(dotIndex+1, template.length());
        return templateType;
    }
    
}
