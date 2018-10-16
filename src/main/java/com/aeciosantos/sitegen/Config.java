package com.aeciosantos.sitegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {

    public String pages_path     = "src/pages";
    public String posts_path     = "src/posts";
    public String templates_path = "src/templates";
    public String static_path    = "src/static";
    public String output_path    = "output/";
    public String server_host    = "127.0.0.1";
    public String base_url       = null;
    public int server_port       = 8080;

    private Config() {

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
        System.out.println("Config.create(): " + config.base_url + " port: " + config.server_port);
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

}