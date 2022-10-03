package com.architecturefirst.boa.technical.util;

import com.architecturefirst.boa.framework.technical.util.JsonSnippet;
import com.architecturefirst.boa.framework.technical.util.ResourceUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Load resources for the project
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResourceUtilsTests.class)
public class ResourceUtilsTests {

    @Test
    public void mapJsonSchemaResources() {
        var utils = new ResourceUtils();
        var map = utils.mapJsonSchemaResources(ResourceUtils.class,
                new ArrayList<String>() {
                    {
                        add("boa-core/json-schema/*.json");
                    }
                });
        var contents = utils.getJsonContentAsString(map,"http://boa.architecture-first.com/json-schema/Acknowledgement");

        new Gson().fromJson(contents, Map.class);
    }

    @Test
    public void listResources() throws IOException {
        var utils = new ResourceUtils();
        var schemaMap = utils.mapJsonSchemaResources();

        for (var entry : schemaMap.entrySet()) {
            System.out.println(utils.getContentsAsString(entry.getValue()));
        }
    }

    @Test
    public void listResourcesSpecifically() throws IOException {
        var utils = new ResourceUtils();
        var resources = utils.getResources(ResourceUtils.class,
                new ArrayList<String>() {
                    {
                        add("boa-core/json-schema/*.json");
                    }
                }
        );

        for (var resource : resources) {
            System.out.println(utils.getContents(resource.getURL()));
        }
    }
}