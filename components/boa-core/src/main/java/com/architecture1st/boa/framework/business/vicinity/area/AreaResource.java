package com.architecture1st.boa.framework.business.vicinity.area;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A resource contained in an area
 */
@AllArgsConstructor
@Data
public class AreaResource {
    private String resourceType;
    private String identifier;
}
