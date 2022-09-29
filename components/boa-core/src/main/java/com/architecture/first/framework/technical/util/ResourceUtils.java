package com.architecture.first.framework.technical.util;

import com.architecture.first.framework.business.vicinity.exceptions.VicinityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Load resources for the project
 */
@Slf4j
public class ResourceUtils {

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

    /**
     * Produce a map of schemas
     * @return
     */
    public Map<String, List<String>> mapJsonSchemaResources(Type cls, List<String> locationPatterns) {
        Map<String, List<String>> mapFiles = new HashMap<>();

        try {
            var resources = getResources(cls, locationPatterns);

            AtomicReference<String> idRef = new AtomicReference<>();
            for (var resource : resources) {
                var contents = getJsonSchemaContents(resource.getFile(), idRef);
                mapFiles.put(idRef.get(), contents);
            }

            var compiledFiles = compileJsonSchemas(mapFiles);
            return compiledFiles;

        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

    }

    public Map<String, List<String>> compileJsonSchemas(Map<String, List<String>> rawSchemas) {
        Map<String, List<String>> mapFiles = new HashMap<>();

        try {
            rawSchemas.entrySet().forEach(es -> {
                List<String> outSchema = new ArrayList<>();
                List<String> inSchema = es.getValue();
                for (String line: inSchema
                     ) {
                    if (line.contains("$ref")) {
                        var id = findID(line);
                        var contents = getJsonContentAsString(rawSchemas, id);
                        // replace string value
                        outSchema.add(contents);
                    }
                    else {
                        outSchema.add(line);
                    }
                }

                mapFiles.put(es.getKey(), outSchema);
            });
        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

        return mapFiles;
    }

    public String getJsonContentAsString(Map<String, List<String>> rawSchemas, String id) {
        var lines = rawSchemas.get(id);
        if (lines != null) {
            return String.join("\n", lines);
        }

        return null;
    }

    public List<String> getJsonSchemaContents(File file, AtomicReference<String> idRef) {

        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

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


    public List<Resource> getResources(Type cls, List<String> locationPatterns) {
        ClassLoader cl = cls.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        List<Resource> resourceList = new ArrayList<>();

        try {
            //var locationPattern = "json-schema/*.json";
            for (String locationPattern : locationPatterns){
                Resource[] resources = resolver.getResources(locationPattern);
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
