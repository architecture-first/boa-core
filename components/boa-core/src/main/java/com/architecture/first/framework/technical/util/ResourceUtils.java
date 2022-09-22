package com.architecture.first.framework.technical.util;

import com.architecture.first.framework.business.vicinity.exceptions.VicinityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
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

    public Map<String, String> mapResources() {
        Map<String, String> mapFiles = new HashMap<>();

        try {
            var resources = getResources();

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
    }
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

    public Map<String, String> mapJsonSchemaResources() {
        Map<String, String> mapFiles = new HashMap<>();

        try {
            var resources = getResources();

            AtomicReference<String> idRef = new AtomicReference<>();
            for (var resource : resources) {
                var contents = getJsonSchemaContents(resource.getFile(), idRef);
                mapFiles.put(idRef.get(), contents);
            }

        }
        catch (Exception e) {
            log.error("Mapping error", e);
            throw new VicinityException(e);
        }

        return mapFiles;
    }

    public String getJsonSchemaContents(File file, AtomicReference<String> idRef) {

        try {
            List<String> lines;
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            lines.forEach( l -> {
                if (l.indexOf("$id") > -1) {
                    Pattern p = Pattern.compile("(?<=([\"']\\b))(?:(?=(\\\\?))\\2.)*?(?=\\1)");
                    Matcher m = p.matcher(l);
                    if (m.find()) {
                        idRef.set(m.group());
                    }
                }
            });

            var contents = String.join("\n", lines);

            return contents;
        }
        catch (Exception e) {
            log.error("Unable to read json schema", e);
            throw new VicinityException(e);
        }
    }

    public List<Resource> getResources() {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        List<Resource> resourceList = new ArrayList<>();

        try {
            var locationPattern = "json-schema/*.json";
            Resource[] resources = resolver.getResources(locationPattern);
            for (Resource r : resources) {
                log.info(r.getFilename());
                resourceList.add(r);
            }
        }
        catch (Exception e) {
            log.error("Unable to read json schema files", e);
            throw new VicinityException(e);
        }

        return resourceList;
    }
}
