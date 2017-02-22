package com.orangepeople.movies.orangepeopletv.Model;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Jan
 * CreateTime:on 2016/10/27.
 */
public class BigVideo implements Serializable {
    private List<BigVideoInfo> bigJson;

    public List<BigVideoInfo> getBigJson() {
        return bigJson;
    }

    public void setBigJson(List<BigVideoInfo> bigJson) {
        this.bigJson = bigJson;
    }
}
