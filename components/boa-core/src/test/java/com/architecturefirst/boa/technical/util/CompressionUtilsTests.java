package com.architecturefirst.boa.technical.util;

import com.architecturefirst.boa.framework.technical.util.CompressionUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CompressionUtilsTests.class)
public class CompressionUtilsTests {

    @Test
    public void compressString() throws Exception {
        var input = "The Architecture-First technique is great great great and Business Oriented Architecture (BOA) is a nice strategy";
        var compressedString = CompressionUtils.compress(input);

        var output = CompressionUtils.decompress(compressedString, input.length());

        Assert.assertEquals(input, output);
    }

    @Test
    public void compressBytes() throws Exception {
        var input = "The Architecture-First technique is great great great and Business Oriented Architecture (BOA) is a nice strategy";
        var compressedString = CompressionUtils.compress(input.getBytes("UTF-8"));

        var output = CompressionUtils.decompress(compressedString, input.length());

        Assert.assertEquals(input, new String(output));
    }


}
