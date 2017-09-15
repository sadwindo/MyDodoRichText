package com.dodo.myrichtext.bean;

import java.io.Serializable;

/**
 * 颜色选择实体类
 * Created by dodo on 2017/7/25.
 */
public class RichColorBean implements Serializable {

    private int color;//颜色值
    private boolean ismake;//是否选中


    public void setColor(int color) {
        this.color = color;
    }

    public void setIsmake(boolean ismake) {
        this.ismake = ismake;
    }

    public int getColor() {
        return color;
    }

    public boolean ismake() {
        return ismake;
    }
}
