package com.wh.starboot.service;

/**
 * Created by  on 2016/7/9.
 */
public interface CaptchaService {

    String recognize(String accessToken, String imageUrl, int length);

    String recognize(byte[] imageArr, int length);

    String getCaptchaImage(String accessToken, String imageUrl);
}
