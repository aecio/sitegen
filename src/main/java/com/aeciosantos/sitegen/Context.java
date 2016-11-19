package com.aeciosantos.sitegen;

import java.util.List;

import com.aeciosantos.sitegen.Main.Site;

public class Context {
    
    public Page page;
    public Site site;
    public Config config;
    public List<Page> pages;

    public Context(Site site, Page page, List<Page> pages) {
        this.site = site;
        this.page = page;
        this.pages = pages;
    }
}