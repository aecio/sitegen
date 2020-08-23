package com.aeciosantos.sitegen;

import com.aeciosantos.sitegen.Main.Site;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class Context {

  public Page page;
  public Site site;
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

  public Page getPage() {
    return page;
  }

  public Site getSite() {
    return site;
  }

  public List<Page> getAllPages() {
    return allPages;
  }

  public List<Page> getPages() {
    return pages;
  }

  public List<Page> getPosts() {
    return posts;
  }

  public String toJson() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize object to JSON.", e);
    }
  }
}
