package com.architecture1st.boa.framework.business.vicinity.area;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor

@Data
public class ActorInArea {
    private String areaName;
    private String actorInfo;

    public boolean isEmpty() {
        return StringUtils.isEmpty(areaName) && StringUtils.isEmpty(actorInfo);
    }

    public static ActorInArea NO_RESULTS() {
        return new ActorInArea(null, null);
    }
}
