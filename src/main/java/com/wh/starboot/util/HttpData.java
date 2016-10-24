package com.wh.starboot.util;

import org.apache.http.impl.client.BasicCookieStore;

/**
 * Created by 掌众 on 2016/10/21.
 */
public class HttpData {

    private int statusCode;
    private String data;
    private BasicCookieStore cookieStore;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public BasicCookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(BasicCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    @Override
    public String toString() {
        return "HttpData{" +
                "statusCode=" + statusCode +
                ", data='" + data + '\'' +
                ", cookieStore=" + cookieStore +
                '}';
    }
}
