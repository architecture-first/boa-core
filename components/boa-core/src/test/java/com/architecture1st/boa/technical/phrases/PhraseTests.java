package com.architecture1st.boa.technical.phrases;

import com.architecture1st.boa.framework.business.vicinity.phrases.Acknowledgement;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Load resources for the project
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PhraseTests.class)
public class PhraseTests {

    @Test
    public void createPhrase() {
        var phrase = new Acknowledgement("0001", "A", "B");
        System.out.println(phrase.toString());
    }
}