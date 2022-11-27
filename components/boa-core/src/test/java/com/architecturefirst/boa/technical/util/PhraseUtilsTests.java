package com.architecturefirst.boa.technical.util;

import com.architecturefirst.boa.framework.technical.util.PhraseUtils;
import com.architecturefirst.boa.framework.technical.util.TranslationUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PhraseUtilsTests.class)
public class PhraseUtilsTests {

    @Test
    public void compareStrings(){
        var source = "   architecture-First business OrientedArchitecture  ";
        var comparee = "Architecture.first    business-Oriented_Architecture ";

        var result = PhraseUtils.isEquivalent(source, comparee);

        Assert.assertTrue(result);
    }

    @Test
    public void checkContains(){
        var source = "   architecture-First business OrientedArchitecture  ";
        var comparee = " business-Oriented_Architecture ";

        var result = PhraseUtils.containsEquivalent(source, comparee);

        Assert.assertTrue(result);
    }
}
