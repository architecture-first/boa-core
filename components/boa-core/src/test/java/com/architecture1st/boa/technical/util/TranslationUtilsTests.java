package com.architecture1st.boa.technical.util;

import com.architecture1st.boa.framework.technical.util.TranslationUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TranslationUtilsTests.class)
public class TranslationUtilsTests {

    @Test
    public void compressString() throws Exception {
        var input = "The Architecture-First technique is great great great and Business Oriented Architecture (BOA) is a nice strategy";
        var compressedString = TranslationUtils.translate(input);

        var output = TranslationUtils.detranslate(compressedString, input.length());

        Assert.assertEquals(input, output);
    }

    @Test
    public void compressBytes() throws Exception {
        var input = "The Architecture-First technique is great great great and Business Oriented Architecture (BOA) is a nice strategy";
        var compressedString = TranslationUtils.translate(input.getBytes("UTF-8"));

        var output = TranslationUtils.detranslate(compressedString, input.length());

        Assert.assertEquals(input, new String(output));
    }


}
