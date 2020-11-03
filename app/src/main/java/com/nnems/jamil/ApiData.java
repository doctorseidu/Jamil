package com.nnems.jamil;

import com.google.gson.JsonObject;

public class ApiData {

    int code;
    String status;
    JsonObject data;

    public ApiData() {
    }

    public ApiData(int code, String status, JsonObject data) {
        this.code = code;
        this.status = status;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
