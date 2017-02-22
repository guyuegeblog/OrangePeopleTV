package com.orangepeople.movies.orangepeopletv.Model;

import java.io.Serializable;

/**
 * Author: wagn
 * CreateTime:on 2016/10/27.
 */

/**
 * 三级视频
 */
public class ThreeVideoInfo implements Serializable {
    public String id;

    public String createtime;

    public String name;

    public String area;

    public String address_sd;

    public String address_hd;

    public String pic;

    public String pic_heng;

    public String lasttime;

    public String score;

    public String showtime;

    public String state;

    public String isLook;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatetime() {
        return createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getAddress_sd() {
        return address_sd;
    }

    public void setAddress_sd(String address_sd) {
        this.address_sd = address_sd;
    }

    public String getAddress_hd() {
        return address_hd;
    }

    public void setAddress_hd(String address_hd) {
        this.address_hd = address_hd;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getPic_heng() {
        return pic_heng;
    }

    public void setPic_heng(String pic_heng) {
        this.pic_heng = pic_heng;
    }

    public String getLasttime() {
        return lasttime;
    }

    public void setLasttime(String lasttime) {
        this.lasttime = lasttime;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIsLook() {
        return isLook;
    }

    public void setIsLook(String isLook) {
        this.isLook = isLook;
    }
}
