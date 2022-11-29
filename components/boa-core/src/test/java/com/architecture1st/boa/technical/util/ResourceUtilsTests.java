package com.architecture1st.boa.technical.util;

import com.architecture1st.boa.framework.technical.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;

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
        var contents = utils.getJsonContentAsString(map,"http://architecture1st.com/json-schema/Acknowledgement");

        System.out.println("output:");
        System.out.println(utils.getResults(contents, "$.properties.header"));
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