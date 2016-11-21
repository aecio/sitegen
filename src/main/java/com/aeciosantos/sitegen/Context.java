package com.aeciosantos.sitegen;

import java.util.List;

import com.aeciosantos.sitegen.Main.Site;

public class Context {
    
    public Page page;
    public Site site;
    public Config config;
    public List<Page> allPages;
    public List<Page> pages;
    public List<Page> posts;

    public Context(Site site, Page page, List<Page> allPages, List<Page> pages, List<Page> posts) {
        this.site = site;
        this.page = page;
        this.allPages = allPages;
        this.pages = pages;
        this.posts = posts;
    }
    
}