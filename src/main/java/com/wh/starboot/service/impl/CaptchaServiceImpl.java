package com.wh.starboot.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wh.starboot.service.CaptchaService;
import com.wh.starboot.util.HttpClientFactory;
import com.wh.starboot.util.MD5;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.stream.FileImageOutputStream;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by kingbo on 2016/7/9.
 */
@Component
public class CaptchaServiceImpl implements CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaServiceImpl.class);

    // 超级鹰
    @Value("${cjy.username}")
    private String cjyUserName;
    @Value("${cjy.password}")
    private String cjyPassword;
    @Value("${cjy.softid}")
    private String cjySoftId;

    @Value("${rk.username}")
    private String rkUserName;
    @Value("${rk.password}")
    private String rkPassword;
    @Value("${rk.softid}")
    private String rkSoftId;
    @Value("${rk.softkey}")
    private String rkSoftKey;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Override
    public String recognize(String accessToken, String imageUrl, int length) {
        String captcha = null;
        try {
            byte[] imageArr = httpClientFactory.downloadImage(accessToken, imageUrl);
            if (imageArr != null) {
                captcha = recognize(imageArr, length);
            }
        } catch (Exception e) {
            logger.info("recognize|exception|imageUrl:{},length:{}", imageUrl, length);
        }
        return captcha;
    }

    @Override
    public String recognize(byte[] imageArr, int length) {
        String captcha = null;
        try {
            //byte2image(imageArr, "d:/work/zz/text.png");
            if (imageArr != null) {
                String codetype = "";
                if (length == 4) {
                    codetype = "1104";
                } else if (length == 5) {
                    codetype = "1005";
                }
                captcha = chaojiying(imageArr, codetype);
            }
        } catch (Exception e) {
            logger.info("recognize|exception|,length:{}", length);
        }
        return captcha;
    }

    @Override
    public String getCaptchaImage(String accessToken, String imageUrl) {
        String captchaImage = null;
        try {
            byte[] imageArr = httpClientFactory.downloadImage(accessToken, imageUrl);
            captchaImage = Base64.encodeBase64String(imageArr);
        } catch (Exception e) {
            logger.error("getCaptchaImage|excetpion|accessToken:{}", accessToken, e);
        }
        return captchaImage;
    }


    private String ruokuaiPostPic(String username, String password, String typeid, String timeout, String softid,
                                  String softkey, byte[] data) throws Exception {
        logger.info("ruokuaiPostPic|Enter the method");
        String result = "";
        String param = String.format("username=%s&password=%s&typeid=%s&timeout=%s&softid=%s&softkey=%s",
                username,
                password,
                typeid,
                timeout,
                softid,
                softkey);
        logger.info("ruokuaiPostPic|param: " + param);
        if (data.length > 0) {
            result = ruokuaiHttpPostImage("http://api.ruokuai.com/create.xml", param, data);
        }
        logger.info("ruokuaiPostPic|Quit the method|result: " + result);
        return result;
    }

    //byte数组到图片
    private static void byte2image(byte[] data, String path) {
        if (data.length < 3 || path.equals("")) return;
        try {
            FileImageOutputStream imageOutput = new FileImageOutputStream(new File(path));
            imageOutput.write(data, 0, data.length);
            imageOutput.close();
            logger.info("Make Picture success,Please find image in " + path);
        } catch (Exception e) {
            logger.error("byte2image|Exception: ", e);
        }
    }

    /**
     * 若快验证
     *
     * @param codetype 1000 任意长度数字
     *                 1040 4位纯数字
     *                 1050 5位纯数字
     *                 1060 6位纯数字
     *                 2000 任意长度字母
     *                 2040 4位纯字母
     *                 2050 5位纯字母
     *                 2060 6位纯字母
     *                 3000 任意长度英数混合
     *                 3040 4位英数混合
     *                 3050 5位英数混合
     *                 3060 6位英数混合
     *                 其他类型码见http://www.ruokuai.com/home/pricetype
     *                 //     * @param url
     * @return
     * @see [类、类#方法、类#成员]
     */
    private String ruokuai(byte[] byteArr, String codetype) {
        logger.info("ruokuai|Enter the method|codetype: " + codetype);
        try {
            String result = ruokuaiPostPic(rkUserName,
                    rkPassword,
                    codetype,
                    "90",
                    rkSoftId,
                    rkSoftKey,
                    byteArr);

            Document doc = DocumentHelper.parseText(result); // 将字符串转为XML
            Element rootEle = doc.getRootElement(); // 获取根节点
            Element resultEle = rootEle.element("Result");
            String captcha = resultEle.getText().trim();
            logger.info("ruokuai|captcha: " + captcha);
            return captcha;
        } catch (Exception e) {
            logger.error("ruokuai| getvcode fail!|codetype: " + codetype, e);
        }
        return null;
    }

    /**
     * 超级鹰验证
     *
     * @param codetype 1104 4位英文数字
     *                 1004 1~4位英文数字
     *                 1005 1~5位英文数字
     *                 1006 1~6位英文数字
     *                 3004 1~4位纯英文
     *                 3005 1~5位纯英文
     *                 3006 1~6位纯英文
     *                 4004 1~4位纯数字
     *                 4005 1~5位纯数字
     *                 4006 1~6位纯数字
     *                 其他类型码见http://www.chaojiying.com/price.html
     *                 //     * @param url
     * @return
     * @see [类、类#方法、类#成员]
     */
    public String chaojiying(byte[] byteArr, String codetype) {
        logger.info("chaojiying|Enter the method|codetype: " + codetype);
        try {
            String result = cjyPostPic(cjyUserName,
                    cjyPassword,
                    cjySoftId,
                    codetype,
                    "0",
                    "0",
                    "ceshi",
                    byteArr);

            JsonObject jsonObj = new Gson().fromJson(result, JsonObject.class);
            try {
                String err_no = jsonObj.get("err_no").getAsString();
                if (StringUtils.equals(err_no, "-1005")) {
                    logger.warn("chaojiying| waring| no money in chaojiying");
                }
            } catch (Exception e) {
                logger.error("chaojiying|get err_no fail!|codetype: " + codetype, e);
            }
            String captcha = jsonObj.get("pic_str").getAsString();
            logger.info("chaojiying|Quit the method|captcha: " + captcha);
            return captcha;
        } catch (Exception e) {
            logger.error("chaojiying|getvcode fail!|codetype: " + codetype, e);
        }
        return null;
    }

    /**
     * 识别图片_按图片二进制流
     *
     * @param username  用户名
     * @param password  密码
     * @param softid    软件ID
     * @param codetype  图片类型
     * @param len_min   最小位数
     * @param time_add  附加时间
     * @param str_debug 开发者自定义信息
     * @param byteArr   图片二进制数据流
     * @return
     * @throws IOException
     */
    private String cjyPostPic(String username, String password, String softid, String codetype, String len_min,
                              String time_add, String str_debug, byte[] byteArr) {
        logger.info("PostPic|Enter the method");
        String result = "";
        String param = String.format("user=%s&pass=%s&softid=%s&codetype=%s&len_min=%s&time_add=%s&str_debug=%s",
                username,
                password,
                softid,
                codetype,
                len_min,
                time_add,
                str_debug);
        logger.info("PostPic|param: " + param);
        try {
            result = cjyHttpPostImage("http://upload.chaojiying.net/Upload/Processing.php", param, byteArr);
        } catch (Exception e) {
            result = "未知问题";
            logger.error("PostPic|chaojiying PostPic failed|param: {}", param, e);
        }
        return result;
    }

    /**
     * 核心上传函数
     *
     * @param url   请求URL
     * @param param 请求参数，如：username=test&password=1
     * @param data  图片二进制流
     * @return response
     * @throws IOException
     */
    public static String cjyHttpPostImage(String url, String param, byte[] data)
            throws IOException {
        logger.info("httpPostImage|Enter the method|url: {}, param: {}", url, param);
        long time = (new Date()).getTime();
        URL u = null;
        HttpURLConnection con = null;
        String boundary = "----------" + MD5.encode(String.valueOf(time));
        String boundarybytesString = "\r\n--" + boundary + "\r\n";
        OutputStream out = null;
        u = new URL(url);
        con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        //con.setReadTimeout(60000);
        con.setConnectTimeout(60000);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(true);
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        out = con.getOutputStream();
        for (String paramValue : param.split("[&]")) {
            out.write(boundarybytesString.getBytes("UTF-8"));
            String paramString = "Content-Disposition: form-data; name=\"" + paramValue.split("[=]")[0] + "\"\r\n\r\n"
                    + paramValue.split("[=]")[1];
            out.write(paramString.getBytes("UTF-8"));
        }
        out.write(boundarybytesString.getBytes("UTF-8"));
        String paramString = "Content-Disposition: form-data; name=\"userfile\"; filename=\"" + "chaojiying_java.gif"
                + "\"\r\nContent-Type: application/octet-stream\r\n\r\n";
        out.write(paramString.getBytes("UTF-8"));
        out.write(data);
        String tailer = "\r\n--" + boundary + "--\r\n";
        out.write(tailer.getBytes("UTF-8"));
        out.flush();
        out.close();
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String temp;
        while ((temp = br.readLine()) != null) {
            buffer.append(temp);
            buffer.append("\n");
        }
        logger.info("httpPostImage|Quit the method|Result: " + buffer);
        return buffer.toString();
    }

    /**
     * 答题
     *
     * @param url   请求URL，不带参数 如：http://api.ruokuai.com/register.xml
     * @param param 请求参数，如：username=test&password=1
     * @param data  图片二进制流
     * @return 平台返回结果XML样式
     * @throws IOException
     */
    public static String ruokuaiHttpPostImage(String url, String param, byte[] data)
            throws IOException {
        logger.info("httpPostImage|Enter the method|url: {}, param: {}", url, param);
        long time = (new Date()).getTime();
        URL u = null;
        HttpURLConnection con = null;
        String boundary = "----------" + MD5.encode(String.valueOf(time));
        String boundarybytesString = "\r\n--" + boundary + "\r\n";
        OutputStream out = null;
        u = new URL(url);
        con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        //con.setReadTimeout(95000);
        con.setConnectTimeout(95000); //此值与timeout参数相关，如果timeout参数是90秒，这里就是95000，建议多5秒
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setUseCaches(true);
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        out = con.getOutputStream();
        for (String paramValue : param.split("[&]")) {
            out.write(boundarybytesString.getBytes("UTF-8"));
            String paramString = "Content-Disposition: form-data; name=\"" + paramValue.split("[=]")[0] + "\"\r\n\r\n"
                    + paramValue.split("[=]")[1];
            out.write(paramString.getBytes("UTF-8"));
        }
        out.write(boundarybytesString.getBytes("UTF-8"));
        String paramString = "Content-Disposition: form-data; name=\"image\"; filename=\"" + "sample.gif"
                + "\"\r\nContent-Type: image/gif\r\n\r\n";
        out.write(paramString.getBytes("UTF-8"));
        out.write(data);
        String tailer = "\r\n--" + boundary + "--\r\n";
        out.write(tailer.getBytes("UTF-8"));
        out.flush();
        out.close();
        StringBuffer buffer = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String temp;
        while ((temp = br.readLine()) != null) {
            buffer.append(temp);
            buffer.append("\n");
        }
        logger.info("httpPostImage|Quti the method|result: " + buffer);
        return buffer.toString();
    }

}
