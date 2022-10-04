package com.architecturefirst.boa.framework.technical.util;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Represents interaction with a JSON snippet
 */
@Data
@RequiredArgsConstructor
public class JsonSnippet {
    @NonNull
    private String snippet;
    private transient Gson gson = new Gson();

    /**
     * The snippet as a Map
     * @return A map version of the snippet
     */
    Map<String,String> toMap() {
        return gson.fromJson(snippet, Map.class);
    }

    /**
     * The snippet as a custom class
     * @param type - The class type of the snippet
     * @return A custom class version of the snippet
     */
    Object toClass(Type type) {
        return gson.fromJson(snippet, type.getClass());
    }

    /**
     * The snippet as a Map
     * @return A map version of the snippet
     */
    JsonSnippet getPathResult(String jsonPath) {
        return JsonSnippet.from(JsonPath.read(snippet, jsonPath));
    }

    /**
     *
     * The JSON snippet
     * @return A string version of the snippet
     */
    public String toString() {
        return snippet;
    }

    /**
     * Produce a snippet from a map
     * @param rawMap - The raw map
     * @return A JsonSnippet
     */
    public static JsonSnippet from(Map<String,String> rawMap) {
        var json = new Gson().toJson(rawMap);
        return new JsonSnippet(json);
    }

    /**
     * Produce a snippet from a map
     * @param data - The data in a give class
     * @return A JsonSnippet
     */
    public static JsonSnippet from(Object data, Type sourceClassType) {
        var json = new Gson().toJson(data, sourceClassType.getClass());
        return new JsonSnippet(json);
    }
}
