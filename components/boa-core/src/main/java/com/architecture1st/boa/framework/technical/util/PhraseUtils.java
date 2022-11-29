package com.architecture1st.boa.framework.technical.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains methods that support friendly String processing
 */
@Slf4j
public class PhraseUtils {

    /**
     * Performs a friendly comparison
     * @param source
     * @param comparee
     * @return
     */
    public static boolean isEquivalent(String source, String comparee) {
        AtomicReference<String> aSource = new AtomicReference<>(source);
        AtomicReference<String> aComparee = new AtomicReference<>(comparee);

        processStrings(aSource, aComparee);

        return aSource.get().equals(aComparee.get());
    }

    /**
     * Performs a friendly heck to see if the source string contains the comparee string
     * @param source
     * @param comparee
     * @return
     */
    public static boolean containsEquivalent(String source, String comparee) {
        AtomicReference<String> aSource = new AtomicReference<>(source);
        AtomicReference<String> aComparee = new AtomicReference<>(comparee);

        processStrings(aSource, aComparee);

        return aSource.get().contains(aComparee.get());
    }

    /**
     * Processes strings to allow for robust comparisons
     * @param aSource
     * @param aComparee
     */
    public static void processStrings(AtomicReference<String> aSource, AtomicReference<String> aComparee) {
        var source = aSource.get();
        var comparee = aComparee.get();
        var src = source.trim();
        var cmp = comparee.trim();

        src = StringUtils.normalizeSpace(src);
        cmp = StringUtils.normalizeSpace(cmp);

        // If source is Camel Case return Title Case
        var words = splitCamelCaseString(src);
        src = StringUtils.join(words, " ");

        // If comparee is Camel Case return Title Case
        words = splitCamelCaseString(cmp);
        cmp = StringUtils.join(words, " ");

        src = src.replaceAll("[\\/\\._\\-\\s]", " ").replaceAll("\\s+", " ");
        cmp = cmp.replaceAll("[\\/\\._\\-\\s]", " ").replaceAll("\\s+", " ");

        src = WordUtils.capitalizeFully(src, ' ','_','.','-');
        cmp = WordUtils.capitalizeFully(cmp, ' ','_','.','-');

        aSource.set(src);
        aComparee.set(cmp);

    }

    public static boolean isCamelCase(String source) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]+([A-Z][a-z]+)+$");
        Matcher matcher = pattern.matcher(source);
        return matcher.matches();
    }

    public static LinkedList<String> splitCamelCaseString(String s){
        LinkedList<String> result = new LinkedList<String>();
        for (String w : s.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            result.add(w);
        }
        return result;
    }
}
