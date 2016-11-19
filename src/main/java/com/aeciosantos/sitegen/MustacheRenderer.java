package com.aeciosantos.sitegen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class MustacheRenderer {
    
    private MustacheFactory mf = new DefaultMustacheFactory();
    
    public void renderToFile(Context context, String template, Path outputFile) throws IOException {
        Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile.toString()));
        renderMustache(context, template, writer);
    }
    
    public String renderToString(Context context, String template) throws IOException {
        StringWriter contentWriter = new StringWriter();
        renderMustache(context, template, contentWriter);
        return contentWriter.toString();
    }

    private void renderMustache(Context context, String template, Writer writer) throws IOException {
        Mustache mustache = mf.compile(new StringReader(template), "page");
        mustache.execute(writer, context);
        writer.flush();
    }
    
}