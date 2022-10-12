package com.architecturefirst.boa.framework.business.vicinity.area;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains settings for an area
 */
@Data
public class AreaConfig {
    private String name;
    private Integer totalCapacityOfActors;
    private Map<String, Integer> neighborAreas = new HashMap<>();   // Name and Distance 1-5
    private Map<String, Integer> totalCapacityOfActorsByType = new HashMap<>();
}
