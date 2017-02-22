package com.orangepeople.movies.orangepeopletv.Model;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Jan
 * CreateTime:on 2016/10/27.
 */
public class ThreeVideo implements Serializable {

    private List<ThreeVideoInfo> threeJson;

    public List<ThreeVideoInfo> getThreeJson() {
        return threeJson;
    }

    public void setThreeJson(List<ThreeVideoInfo> threeJson) {
        this.threeJson = threeJson;
    }
}
