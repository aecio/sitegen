package com.aeciosantos.sitegen;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import java.io.File;
import java.nio.file.Paths;

public class WebServer {

  private final Config config;

  public WebServer(Config config) {
    this.config = config;
  }

  public void startWebServer() {
    int port = config.server_port;
    String host = config.server_host;
    File serverPath = Paths.get(config.output_path).toFile();

    ResourceHandler handler = new ResourceHandler(new PathResourceManager(serverPath.toPath()));
    Undertow server =
        Undertow.builder()
            .addHttpListener(port, host)
            .setHandler(Handlers.path().addPrefixPath("/", handler))
            .build();
    server.start();

    System.out.println("Website available at http://" + host + ":" + port);
  }
}
