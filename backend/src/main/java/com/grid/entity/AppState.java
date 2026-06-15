package com.grid.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("app_state")
public class AppState {
    @TableId
    private String stateKey;
    private String stateValue;

    public AppState() {
    }

    public AppState(String stateKey, String stateValue) {
        this.stateKey = stateKey;
        this.stateValue = stateValue;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getStateValue() {
        return stateValue;
    }

    public void setStateValue(String stateValue) {
        this.stateValue = stateValue;
    }
}
