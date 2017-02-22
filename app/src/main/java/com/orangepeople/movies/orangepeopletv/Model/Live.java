package com.orangepeople.movies.orangepeopletv.Model;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Jan
 * CreateTime:on 2016/10/27.
 */
public class Live implements Serializable {
    private List<LiveInfo> liveJson;

    public List<LiveInfo> getLiveJson() {
        return liveJson;
    }

    public void setLiveJson(List<LiveInfo> liveJson) {
        this.liveJson = liveJson;
    }
}
