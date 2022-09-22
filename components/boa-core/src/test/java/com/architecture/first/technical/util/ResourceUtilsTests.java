package com.architecture.first.technical.util;

import com.architecture.first.framework.business.vicinity.exceptions.VicinityException;
import com.architecture.first.framework.technical.util.ResourceUtils;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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
        var map = utils.mapJsonSchemaResources();

        new Gson().fromJson(map.get("http://boa.architecture-first.com/schema/boa-common"), Map.class);
    }

    @Test
    public void mapResources() {
        var utils = new ResourceUtils();
        var map = utils.mapResources();
    }

    @Test
    public void listResources() throws IOException {
        var utils = new ResourceUtils();
        var resources = utils.getResources();

        for (var resource : resources) {
            utils.getContents(resource.getFile());
        }

    }
}