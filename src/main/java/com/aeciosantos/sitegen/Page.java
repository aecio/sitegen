package com.aeciosantos.sitegen;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Page {
    
    private static final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
    
    public static final Comparator<Page> DESC = (Page p1, Page p2) -> p2.published_time.compareTo(p1.published_time);
    
    public String template = "default.mustache";
    public String title = "";
    public String author = "";
    public String description = "";
    public String permalink = null;
    public String content_type = "mustache";
    public String content = "";
    public Date published_time = new Date();
    public Date modified_time  = new Date();
    
    public String getPublicationDay() {
        return new SimpleDateFormat("MMMMM dd, YYYY").format(published_time);
    }
    
    public String getPublicationHour() {
        return new SimpleDateFormat("hh:mm aaa").format(published_time);
    }
    
    public static List<Page> loadPages(Path pagesPath) throws IOException {
        List<Page> pages = new ArrayList<Page>();
        if(!Files.isDirectory(pagesPath)) {
            return pages;
        }
        try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(pagesPath)) {
            for (Path file : directoryStream) {
                if(Files.isDirectory(file)) {
                    pages.addAll(loadPages(file));
                } else {
                    System.out.println("Loading page file: "+file.toString());
                    Page page = Page.fromFile(file);
                    if(page == null) {
                        System.err.println("ERROR: Could not parse page file: "+file.toString());
                        continue;
                    }
                    pages.add(page);
                }
            }
        }
        return pages;
    }

    public static Page fromFile(Path file) throws IOException,
                                                  JsonParseException,
                                                  JsonMappingException {

        List<String> fileLines = Files.readAllLines(file);

        Page page = null;
        int markerCount = 0;
        StringBuilder builder = new StringBuilder();
        for (String line : fileLines) {
            if ("---".equals(line.trim())) {
                markerCount++;
                if (markerCount == 2) {
                    String metadata = builder.toString();
                    page = yaml.readValue(metadata, Page.class);
                    builder = new StringBuilder();
                }
            } else {
                builder.append(line);
                builder.append('\n');
            }
        }

        if (builder.length() > 0 && page != null) {
            page.content = builder.toString();
        }

        return page;
    }
}
