package com.architecturefirst.boa.framework.technical.util;

import com.architecturefirst.boa.framework.business.vicinity.exceptions.VicinityException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Load resources for the project
 */
@Slf4j
public class ResourceUtils {

    private Gson gson = new Gson();

/*    public Map<String, String> mapResources() {
        Map<String, String> mapFiles = new HashMap<>();

        try {
            var resources = getResources(this.getClass());

            for (var resource : resources) {
                var contents = getContents(resource.getFile());
                mapFiles.put(resource.getFilename(), contents);
            }

        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

        return mapFiles;
    }*/
    public String getContents(File file) {
        try {
            List<String> lines;
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            var contents = String.join("\n", lines);

            return contents;
        }
        catch (Exception e) {
            log.error("Unable to read json schema", e);
            throw new VicinityException(e);
        }
    }

    public String getContents(URL url) {

        BufferedReader br = null;
        String str = null;
        StringBuilder strb = new StringBuilder();

        try {
            br = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            while ((str = br.readLine()) != null) {
                strb.append(str).append("\n");
            }

            return strb.toString();
        }
        catch (Exception e) {
            log.error("Unable to read json schema", e);
            throw new VicinityException(e);
        }
        finally {
            if (br != null) {
                try {
                    br.close();
                }
                catch (IOException e) {
                    throw new VicinityException(e);
                }
            }
        }
    }

    public List<String> getContentsAsLines(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            return lines;
        }
        catch (Exception e) {
            log.error("Unable to read json schema", e);
            throw new VicinityException(e);
        }
    }

    public String getContentsAsString(List<String> lines) {
        return String.join("\n", lines);
    }

    public String getContentsAsString(Map<String,String> lines) {
        return gson.toJson(lines);
    }


    /**
     * Produce a map of schemas
     * @return
     */
    public Map<String, Map<String,String>> mapJsonSchemaResources() {
        Map<String, List<String>> mapFiles = new HashMap<>();

        try {
            var resources = getResources();
            return compileResources(mapFiles, resources);

        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

    }

    private Map<String, Map<String,String>> compileResources(Map<String, List<String>> mapFiles, List<Resource> resources) throws IOException {
        AtomicReference<String> idRef = new AtomicReference<>();
        for (var resource : resources) {
            log.info("Processing resource: " + resource.getURL());
            var contents = getJsonSchemaContents(resource, idRef);
            mapFiles.put(idRef.get(), contents);
        }

        var compiledFiles = compileJsonSchemas(mapFiles);
        return compiledFiles;
    }

    /**
     * Produce a map of schemas
     * @return
     */
    public Map<String, Map<String,String>> mapJsonSchemaResources(Type cls, List<String> locationPatterns) {
        Map<String, List<String>> mapFiles = new HashMap<>();

        try {
            var resources = getResources(cls, locationPatterns);
            return compileResources(mapFiles, resources);
        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

    }

    public Map<String, Map<String,String>> compileJsonSchemas(Map<String, List<String>> rawSchemas) {
        Map<String, Map<String,String>> mapCompiled = new LinkedHashMap<>();

        try {
            rawSchemas.entrySet().forEach(es -> {
                List<String> outSchema = new ArrayList<>();
                List<String> inSchema = es.getValue();
                for (String line: inSchema
                     ) {
                    if (line.contains("$ref")) {
                        var id = findID(line);
                        var contents = getRawJsonContentAsString(rawSchemas, id)
                                .replace("\n", "");
                        // replace string value
                        outSchema.add(contents);
                    }
                    else {
                        outSchema.add(line);
                    }
                }

                String workData = String.join(" ", outSchema);
                var compilation = gson.fromJson(workData, Map.class);
                mapCompiled.put(es.getKey(), compilation);
            });
        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

        return mapCompiled;
    }

    public String getRawJsonContentAsString(Map<String, List<String>> rawSchemas, String id) {
        var lines = rawSchemas.get(id);
        var workLines = new ArrayList();

        // Skip starting and closing braces since nested in parent
        for (int i=1; i<lines.size()-1; i++) {
            workLines.add(lines.get(i));
        }
        if (lines != null) {
            return String.join("\n", workLines);
        }

        return null;
    }

    public String getJsonContentAsString(Map<String, Map<String,String>> rawSchemas, String id) {
        var lines = rawSchemas.get(id);
        if (lines != null) {
            return getContentsAsString(lines);
        }

        return null;
    }

    public List<String> getJsonSchemaContents(Resource resource, AtomicReference<String> idRef) {

        try {
            String contents = getContents(resource.getURL());
            List<String> lines = List.of(contents.split("\\n"));

            lines.forEach( l -> {
                if (l.indexOf("$id") > -1) {
                    findID(idRef, l);
                }
            });

            return lines;
        }
        catch (Exception e) {
            log.error("Unable to read json schema", e);
            throw new VicinityException(e);
        }
    }

    private String findID(String l) {
        Pattern p = Pattern.compile("(?<=([\"']\\b))(?:(?=(\\\\?))\\2.)*?(?=\\1)");
        Matcher m = p.matcher(l);
        if (m.find()) {
            return m.group();
        }

        return null;
    }

    private void findID(AtomicReference<String> idRef, String l) {
        Pattern p = Pattern.compile("(?<=([\"']\\b))(?:(?=(\\\\?))\\2.)*?(?=\\1)");
        Matcher m = p.matcher(l);
        if (m.find()) {
            idRef.set(m.group());
        }
    }


    protected void onAddResourceLocationPattern(List<String> locationPatterns) {
        locationPatterns.add("boa-core/json-schema/*.json");
        locationPatterns.add("boa-actor/json-schema/*.json");
        locationPatterns.add("my-actor/json-schema/*.json");
    }

    public List<Resource> getResources() {
        var list = new ArrayList<String>();
        onAddResourceLocationPattern(list);

        return getResources(this.getClass(), list);
    }

    public List<Resource> getResources(Type cls, List<String> locationPatterns) {
        ClassLoader cl = cls.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        List<Resource> resourceList = new ArrayList<>();

        try {
            for (String locationPattern : locationPatterns){
                Resource[] resources = null;
                try {
                    resources = resolver.getResources(locationPattern);
                }
                catch (Exception e) {
                    // Do not fail for optional JSON Schema information
                    log.warn("JSON Schema not found: " + e.getMessage());
                    continue;
                }
                for (Resource r : resources) {
                    log.info(r.getFilename());
                    resourceList.add(r);
                }
            }
        }
        catch (Exception e) {
            log.error("Unable to read json schema files", e);
            throw new VicinityException(e);
        }

        return resourceList;
    }
}
