package com.aeciosantos.sitegen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public class Main {

	private static ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
	private static MustacheRenderer mustache = new MustacheRenderer();
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("Starting site generator...");
		
		Config config = new Config();
		Site site = new Site("http://localhost:3000");
//		Site site = new Site(Paths.get(config.output_path).toAbsolutePath().toString());
		
		Files.createDirectories(Paths.get(config.output_path).getParent());
		
		List<Page> pages = loadPages(config.pages_path);
		Map<String, String> templateFiles = loadTemplates(config.templates_path);
		
		for(Page page : pages) {
			
			Context context = new Context(site, page);
			
			if("mustache".equals(page.content_type)) {
				page.content = mustache.renderToString(context, page.content);
			}
			
			String template = templateFiles.get(page.template);
			if(template == null) {
				System.err.println("Specified template not found: "+page.template);
			}
			
			String templateType = getTemplateType(page.template);
			if("mustache".equals(templateType)) {
				Path generated = Paths.get(config.output_path, ".", page.permalink);
				System.out.println("Rendering page: "+generated.toString());
				Files.createDirectories(generated.getParent());
				mustache.renderToFile(context, template, generated);
			} else {
				System.err.println("No template engine found for type: "+templateType);
			}
		}
		
		System.out.println("Copying static folder...");
		FileUtils.copyDirectory(
			Paths.get(config.static_path).toFile(),
			Paths.get(config.output_path, "static").toFile()
		);
		
		System.out.println("Generation finished.");
	}

	private static String getTemplateType(String template) {
		int dotIndex = template.lastIndexOf('.');
		String templateType = template.substring(dotIndex+1, template.length());
		return templateType;
	}

	private static Map<String, String> loadTemplates(String templatesPath) throws IOException {
		DirectoryStream<Path> path = Files.newDirectoryStream(Paths.get(templatesPath));
		Map<String, String> templates = new HashMap<String, String>();
		for (Path file : path) {
			System.out.println("Loading template file: "+file.toString());
			byte[] template = Files.readAllBytes(file);
			templates.put(file.getFileName().toString(), new String(template));
		}
		return templates;
	}

	private static List<Page> loadPages(String pagesPath) throws IOException, JsonParseException, JsonMappingException {
		DirectoryStream<Path> path = Files.newDirectoryStream(Paths.get(pagesPath));
		List<Page> pages = new ArrayList<Page>();
		for (Path file : path) {
			System.out.println("Loading page file: "+file.toString());
			Page page = parsePageFile(file);
			if(page == null) {
				System.err.println("Could not parse page file: "+file.toString());
				continue;
			}
			pages.add(page);
		}
		return pages;
	}
	
	static class MustacheRenderer {
		
		private MustacheFactory mf = new DefaultMustacheFactory();
		
		public void renderToFile(Context context, String template, Path generated) throws IOException {
			Writer writer = new OutputStreamWriter(new FileOutputStream(generated.toString()));
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

	private static Page parsePageFile(Path file) throws IOException,
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

	static public class Config {
		public String pages_path     = "src/pages";
		public String templates_path = "src/templates";
		public String static_path    = "src/static";
		public String output_path    = "output/";
	}

	static public class Context {
		public Page page;
		public Site site;

		public Context(Site site, Page page) {
			this.site = site;
			this.page = page;
		}
	}

	static public class Site {
		public Site(String baseUrl) {
			base_url = baseUrl;
		}

		public String base_url;
	}

	static public class Page {
		public String template = "default.mustache";
		public String title = "";
		public String author = "";
		public String description = "";
		public String permalink = null;
		public String content_type = "mustache";
		public String content = "";
	}

}
