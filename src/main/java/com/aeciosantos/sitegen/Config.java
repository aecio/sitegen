package com.aeciosantos.sitegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    public String pages_path = "src/pages";
    public String posts_path = "src/posts";
    public String templates_path = "src/templates";
    public String static_path = "src/static";
    public String output_path = "output/";
    public String server_host = "127.0.0.1";
    public String base_url = null;
    public int server_port = 8080;

    private Config() {
        // required for object deserialization from YAML
    }

    public static Config create() {

        Config config = readFromFile("./sitegen.yml");

        if (config == null) {
            config = readFromFile("./sitegen.yaml");
        }

        if (config == null) {
            config = new Config();
        }

        if (config.base_url == null) {
            config.base_url = "http://" + config.server_host + ":" + config.server_port;
        }

        return config;
    }

    private static Config readFromFile(String configPath) {
        if (configPath == null) {
            return null;
        }

        Path path = Paths.get(configPath);
        if (!Files.exists(path)) {
            return null;
        }

        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        try {
            return yaml.readValue(Files.readAllBytes(path), Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed read or parse config file: " + configPath);
        }
    }

    public ConfigPaths getPaths() {
        return new ConfigPaths(this);
    }

    public static class ConfigPaths {

        public final Path templates;
        public final Path posts;
        public final Path pages;
        public final Path pagesOutput;
        public final Path staticFiles;
        public final Path staticFilesOutput;

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