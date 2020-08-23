package com.aeciosantos.sitegen.renderer;

import com.aeciosantos.sitegen.Context;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class FreemakerRenderer {

  private HashFunction md5 = Hashing.md5();

  public void renderToFile(Context context, String template, Path outputFile) throws IOException {
    Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile.toString()));
    renderFreemaker(context, template, writer);
    writer.close();
  }

  public String renderToString(Context context, String template) throws IOException {
    StringWriter contentWriter = new StringWriter();
    renderFreemaker(context, template, contentWriter);
    return contentWriter.toString();
  }

  private void renderFreemaker(Context context, String template, Writer writer) throws IOException {

    String hash = md5.newHasher().putString(template, StandardCharsets.UTF_8).hash().toString();
    String templateName = hash;

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate(hash, template);

    Configuration cfg = createConfig();
    cfg.setTemplateLoader(stringLoader);
    Template freemakerTemplate = cfg.getTemplate(templateName);

    try {
      freemakerTemplate.process(context, writer);
    } catch (TemplateException e) {
      throw new IOException("Failed to render freemaker template.", e);
    }
  }

  private Configuration createConfig() throws IOException {
    // Create your Configuration instance, and specify if up to what FreeMarker
    // version (here 2.3.27) do you want to apply the fixes that are not 100%
    // backward-compatible. See the Configuration JavaDoc for details.
    Configuration cfg = new Configuration(Configuration.VERSION_2_3_27);
    cfg.setDefaultEncoding("UTF-8");

    // Sets how errors will appear.
    // During web page *development* TemplateExceptionHandler.HTML_DEBUG_HANDLER is better.
    // cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

    // Don't log exceptions inside FreeMarker that it will thrown at you anyway:
    cfg.setLogTemplateExceptions(false);

    // Wrap unchecked exceptions thrown during template processing into TemplateException-s.
    cfg.setWrapUncheckedExceptions(true);

    return cfg;
  }
}
