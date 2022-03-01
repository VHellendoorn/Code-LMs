package com.bilibili.model.bean;

/**
 * Created by miserydx on 17/6/30.
 */

public class ResultObjectResponse<T> {

    private int code;

    private T result;

    private String message;

    private int ttl;

    private String ver;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return result;
    }

    public void setData(T data) {
        this.result = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }
}
