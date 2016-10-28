package com.wh.starboot.util;

import org.apache.http.Header;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by  on 2016/10/21.
 */
public class HttpData {

    private int statusCode;
    private String data;
    private BasicCookieStore cookieStore;
    private Header[] headers;

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

    public Header[] getHeaders() {
        return headers;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "HttpData{" +
                "statusCode=" + statusCode +
                ", data='" + data + '\'' +
                '}';
    }

    public Header[] getHeaders(final String name) {
        List<Header> headersFound = null;
        for (int i = 0; i < this.headers.length; i++) {
            final Header header = this.headers[i];
            if (header.getName().equalsIgnoreCase(name)) {
                if (headersFound == null) {
                    headersFound = new ArrayList<Header>();
                }
                headersFound.add(header);
            }
        }
        return headersFound != null ? headersFound.toArray(new Header[headersFound.size()]) : new Header[]{};
    }


    public Header getFirstHeader(final String name) {
        for (int i = 0; i < this.headers.length; i++) {
            final Header header = this.headers[i];
            if (header.getName().equalsIgnoreCase(name)) {
                return header;
            }
        }
        return null;
    }

}
