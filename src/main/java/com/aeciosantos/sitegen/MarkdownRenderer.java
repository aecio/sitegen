package com.aeciosantos.sitegen;

import java.io.IOException;
import java.nio.file.Path;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import com.google.common.io.Files;

public class MarkdownRenderer {

    public void renderToFile(Context context, String template, Path outputFile) throws IOException {
        String html = renderToString(context, template);
        Files.write(html.getBytes(), outputFile.toFile());
    }

    public String renderToString(Context context, String template) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(template);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

}
