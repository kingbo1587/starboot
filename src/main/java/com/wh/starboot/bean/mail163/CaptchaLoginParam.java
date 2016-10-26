package com.wh.starboot.bean.mail163;

/**
 * Created by  on 2016/8/18.
 */
public class CaptchaLoginParam {

    private String loginCookie;
    private String sInfoCookie;
    private String pInfoCookie;
    private String antiCsrfCookie;

    public String getLoginCookie() {
        return loginCookie;
    }

    public void setLoginCookie(String loginCookie) {
        this.loginCookie = loginCookie;
    }

    public String getsInfoCookie() {
        return sInfoCookie;
    }

    public void setsInfoCookie(String sInfoCookie) {
        this.sInfoCookie = sInfoCookie;
    }

    public String getpInfoCookie() {
        return pInfoCookie;
    }

    public void setpInfoCookie(String pInfoCookie) {
        this.pInfoCookie = pInfoCookie;
    }

    public String getAntiCsrfCookie() {
        return antiCsrfCookie;
    }

    public void setAntiCsrfCookie(String antiCsrfCookie) {
        this.antiCsrfCookie = antiCsrfCookie;
    }

    @Override
    public String toString() {
        return "CaptchaLoginParam{" +
                "loginCookie='" + loginCookie + '\'' +
                ", sInfoCookie='" + sInfoCookie + '\'' +
                ", pInfoCookie='" + pInfoCookie + '\'' +
                ", antiCsrfCookie='" + antiCsrfCookie + '\'' +
                '}';
    }
}
