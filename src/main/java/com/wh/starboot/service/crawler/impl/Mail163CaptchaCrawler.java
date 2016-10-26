package com.wh.starboot.service.crawler.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wh.starboot.bean.MailInfo;
import com.wh.starboot.bean.MailTask;
import com.wh.starboot.bean.TaskStatus;
import com.wh.starboot.bean.mail163.Mail163Info;
import com.wh.starboot.bean.mail163.Mail163ListResp;
import com.wh.starboot.dao.MailDao;
import com.wh.starboot.service.CaptchaService;
import com.wh.starboot.service.crawler.CrawlerHelper;
import com.wh.starboot.service.crawler.MailCrawler;
import com.wh.starboot.util.Consts;
import com.wh.starboot.util.HttpClientFactory;
import com.wh.starboot.util.HttpData;
import com.wh.starboot.util.MyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shenjinbo on 2016/7/16.
 */
@Service
public class Mail163CaptchaCrawler implements MailCrawler {

    private static final Logger logger = LoggerFactory.getLogger(Mail163CaptchaCrawler.class);

    /**
     * 登录url
     */
    private static final String LOGIN_URL = "https://mail.MAIL_TYPE/entry/cgi/ntesdoor?funcid=loginone&language=-1&passtype=1&iframe=1&product=mail163&from=web&df=email163&race=-2_100_-2_hz&module=&uid=UID&style=-1&net=t&skinid=null";

    private static final String CAPTCHA_ID_URL = "https://reg.163.com/services/getid";

    private static final String CAPTCHA_URL = "https://reg.163.com/services/getimg?id=ID";

    private static final String SUBMIT_CAPTCHA_URL = "https://reg.163.com/services/checkcode?filledVerifyID=CAPTCHA&sysVerifyID=SYS_VERIFY_ID&isLoginException=1";

    /**
     * 邮件列表
     */
    private static final String MAIL_LIST_URL = "http://mail.MAIL_TYPE/js6/s?sid=SID&func=mbox:listMessages";
    /**
     * 邮件信息
     */
    private static final String MAIL_URL = "http://mail.MAIL_TYPE/js6/read/readhtml.jsp?mid=MID&font=15&color=064977";

    private final SimpleDateFormat sdfParser = new SimpleDateFormat("yyyy,M,d,HH,m,s");
    private final SimpleDateFormat sdfDateParser = new SimpleDateFormat("yyyy,M,d");
    private final SimpleDateFormat sdfFormatter = new SimpleDateFormat("yyyy" + Consts.DATE_SEPARATOR + "MM" + Consts.DATE_SEPARATOR + "dd HH:mm:ss");
    /**
     * 爬取页容量
     */
    private static final int CRAWL_PAGE_SIZE = 50;

    @Autowired
    private HttpClientFactory httpClientFactory;
    @Autowired
    private MailDao mailDao;
    @Autowired
    private CaptchaService captchaService;

    @Override
    public Object loginMail(MailTask mailTask) {
        String taskid = mailTask.getTaskid();
        logger.info("loginMail|Enter the method|taskid:{},account:{}", taskid, mailTask.getAccount());
        String loginResp = "";
        if (StringUtils.isBlank(mailTask.getCaptcha())) {
            // 正常登录
            loginResp = loginRequest(mailTask);
            if (StringUtils.isBlank(loginResp)) {
                return null;
            }
            if (loginResp.contains("errorType=460")) {
                // 帐号密码错误
                mailDao.updateTask(mailTask, TaskStatus.LOGIN_USRPASS_ERROR);
                return null;
            }
            if (loginResp.contains("<title>relogin</title>")) {
                // 需要打码
                String captchaPageUrl = MyUtil.getBetweenValue(loginResp, "action=\"", "\" method");
                boolean visitOk = requestCaptchaPage(mailTask, captchaPageUrl);
                if (visitOk) {
                    // 打码
                    String verifyId = getVerifyId(taskid);
                    if (StringUtils.isNotBlank(verifyId)) {
                        String captchaImage = getCaptchaImage(taskid, verifyId);
                        if (StringUtils.isBlank(captchaImage)) {
                            // 获取验证码异常
                            mailDao.updateTask(mailTask, TaskStatus.LOGIN_ERROR);
                            return null;
                        }
                        // 需要验证码
                        mailDao.updateTask(mailTask, captchaImage, verifyId, TaskStatus.LOGIN_NEED_CAPTCHA);
                        return null;
                    }
                }
            }
        } else {
            String verifyId = mailTask.getLoginParam();
            String captcha = mailTask.getCaptcha();
            // 验证码登录
            String captchaCode = submitCaptcha(taskid, verifyId, captcha);
            if ("200".equals(captchaCode)) {
                // 打码成功再次登录
                loginResp = loginRequest(mailTask);
            } else if ("461".equals(captchaCode)) {
                String captchaImage = getCaptchaImage(taskid, verifyId);
                if (StringUtils.isBlank(captchaImage)) {
                    // 获取验证码异常
                    mailDao.updateTask(mailTask, TaskStatus.LOGIN_ERROR);
                    return null;
                }
                // 需要验证码
                mailDao.updateTask(mailTask, captchaImage, verifyId, TaskStatus.LOGIN_CAPTCHA_ERROR);
                return null;
            }
        }
        String sid = null;
        //对结果进行解析， 如果正确的话，则进行下一步
        List<String> sids = MyUtil.getMatchers("(?<=sid=).+(?=&)", loginResp);
        if (!sids.isEmpty()) {
            sid = sids.get(0);
        }
        if (StringUtils.isBlank(sid)) {
            //更新mongo状态为登入失败
            mailDao.updateTask(mailTask, TaskStatus.LOGIN_ERROR);
        } else {
            mailDao.updateTask(mailTask, TaskStatus.LOGIN_SUCCESS);
        }
        return sid;
    }

    @Override
    public List<MailInfo> crawlMails(MailTask mailTask, Object loginData) {
        logger.info("crawlMails|Enter the method|taskid:{},loginData:{}", mailTask.getTaskid(), loginData);
        String sid = (String) loginData;
        List<MailInfo> mailInfos = new ArrayList<>();
        if (StringUtils.isNotBlank(sid)) {
            List<Mail163Info> mail163InfoList = getMailList(mailTask, sid);
            if (mail163InfoList != null && !mail163InfoList.isEmpty()) {
                for (Mail163Info mail163Info : mail163InfoList) {
                    String mid = mail163Info.getId();
                    // 获取邮件内容数据
                    String mailData = getMail(mailTask, sid, mid);
                    MailInfo mailInfo = new MailInfo();
                    mailInfos.add(mailInfo);
                    mailInfo.setTaskid(mailTask.getTaskid());
                    mailInfo.setAccount(mailTask.getAccount());
                    mailInfo.setUserid(mailTask.getUserid());
                    mailInfo.setMailType(mailTask.getType());
                    mailInfo.setFrom(mail163Info.getFrom());
                    mailInfo.setTo(mail163Info.getTo());
                    mailInfo.setSubject(mail163Info.getSubject());
                    mailInfo.setSentDate(mail163Info.getSentDate());
                    mailInfo.setReceivedDate(mail163Info.getReceivedDate());
                    mailInfo.setMailData(mailData);
                }
            }
        }
        return mailInfos;
    }

    @Override
    public void setCrawlTime(MailTask mailTask, long crawlTime) {
        mailTask.setCrawlTime(crawlTime);
    }

    private String loginRequest(MailTask mailTask) {
        String taskid = mailTask.getTaskid();
        logger.info("loginRequest|Enter method|taskid:{},captcha:{}", taskid, mailTask.getCaptcha());
        String mailType = mailTask.getType();
        String url = LOGIN_URL.replace("MAIL_TYPE", mailType).replace("UID", mailTask.getAccount());
        HttpPost httpPost = new HttpPost(url);
        // 请求头
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Host", "mail." + mailType);
        httpPost.addHeader("Referer", "http://email.163.com");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // 请求参数
        List<NameValuePair> postparms = new ArrayList<>();
        postparms.add(new BasicNameValuePair("password", mailTask.getPassword()));
        postparms.add(new BasicNameValuePair("savalogin", "0"));
        postparms.add(new BasicNameValuePair("url2", "http://email.163.com/errorpage/error163.htm"));
        postparms.add(new BasicNameValuePair("username", mailTask.getAccount()));


        // 登录
        String entityStr = null;
        try {
            HttpEntity paramEntity = new UrlEncodedFormEntity(postparms);
            httpPost.setEntity(paramEntity);
            HttpData response = httpClientFactory.execute(taskid, httpPost);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.info("login|fail|taskid:{},statusCode:{}", taskid, response.getStatusCode());
                return null;
            }
            entityStr = response.getData();
            logger.debug("login|taskid:{},entityStr:{}", taskid, entityStr);
        } catch (Exception e) {
            logger.error("loginMail|exception|taskid:{},account:{}", mailTask.getTaskid(), mailTask.getAccount(), e);
        }
        return entityStr;
    }

    /**
     * 请求安全验证访问页面
     *
     * @param mailTask 邮件任务
     * @param url      安全页面
     * @return 是否请求成功
     */
    private boolean requestCaptchaPage(MailTask mailTask, String url) {
        String taskid = mailTask.getTaskid();
        logger.info("requestCaptchaPage|Enter the methdo|taskid:{},url:{}", taskid, url);
        HttpPost httpPost = new HttpPost(url);
        // 请求头
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpPost.addHeader("Cache-Control", "max-age=0");
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Host", "reg.163.com");
        httpPost.addHeader("Upgrade-Insecure-Requests", "1");
        // 请求参数
        List<NameValuePair> postparms = new ArrayList<>();
        postparms.add(new BasicNameValuePair("username", mailTask.getAccount()));
        postparms.add(new BasicNameValuePair("password", mailTask.getPassword()));
        HttpData response = null;
        boolean visitOk = false;
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(postparms));
            response = httpClientFactory.execute(taskid, httpPost);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                logger.info("requestCaptchaPage|fail|taskid:{},statusCode:{}", taskid, response.getStatusCode());
            } else {
                visitOk = true;
            }
        } catch (Exception e) {
            logger.error("requestCaptchaPage|exception|taskid:{},response:{}", taskid, response, e);
        }
        return visitOk;
    }

    /**
     * 获取验证id
     *
     * @param taskid 任务编号
     * @return verifyId
     */
    private String getVerifyId(String taskid) {
        logger.info("requestCaptchaPage|Enter the methdo|taskid:{},url:{}", taskid);
        String url = CAPTCHA_ID_URL;
        HttpGet httpGet = new HttpGet(url);
        // 请求头
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate, sdch, br");
        httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpGet.addHeader("Host", "reg.163.com");
        httpGet.addHeader("X-Prototype-Version", "1.4.0");
        httpGet.addHeader("X-Requested-With", "XMLHttpRequest");
        HttpData response = null;
        String verifyId = null;
        try {
            response = httpClientFactory.execute(taskid, httpGet);
            logger.debug("getVerifyId|response|taskid:{},status:{},entityStr:{}", taskid, response.getStatusCode(), response.getData());
            verifyId = response.getData();
        } catch (Exception e) {
            logger.error("requestCaptchaPage|exception|taskid:{},response:{}", taskid, response, e);
        }
        return verifyId;
    }

    /**
     * 提交验证码
     *
     * @param taskid   任务编号
     * @param verifyId 验证id
     * @param captcha  验证码
     * @return 是否提交成功
     */
    private String submitCaptcha(String taskid, String verifyId, String captcha) {
        logger.info("submitCaptcha|Enter the methdo|taskid:{},verifyId:{},captcha:{}", taskid, verifyId, captcha);
        String url = SUBMIT_CAPTCHA_URL.replace("CAPTCHA", captcha).replace("SYS_VERIFY_ID", verifyId);
        HttpGet httpGet = new HttpGet(url);
        // 请求头
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate, sdch, br");
        httpGet.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
        httpGet.addHeader("Connection", "keep-alive");
        httpGet.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpGet.addHeader("Host", "reg.163.com");
        httpGet.addHeader("X-Prototype-Version", "1.4.0");
        httpGet.addHeader("X-Requested-With", "XMLHttpRequest");
        HttpData response = null;
        String captchaCode = "";
        try {
            response = httpClientFactory.execute(taskid, httpGet);
            String entityStr = response.getData();
            logger.debug("submitCaptcha|response|taskid:{},statusCode:{},entityStr:{}", taskid, response.getStatusCode(), entityStr);
            if (response.getStatusCode() == HttpStatus.SC_OK) {
                if (entityStr.contains("200")) {
                    captchaCode = "200";
                } else {
                    captchaCode = entityStr.trim();
                }
            }
        } catch (Exception e) {
            logger.error("submitCaptcha|exception|taskid:{},response:{}", taskid, response, e);
            captchaCode = "";
        }
        return captchaCode;
    }

    /**
     * 打码
     *
     * @param taskid 任务编号
     * @return 验证码
     */
//    private String getCaptcha(String taskid, String captchaId) {
//        logger.info("getCaptcha|taskid:{},captchaId:{}", taskid, captchaId);
//        String url = CAPTCHA_URL.replace("ID", captchaId);
//        return captchaService.recognize(taskid, url, 5);
//    }

    /**
     * 获取验证码图片
     *
     * @param taskid    任务编号
     * @param captchaId 验证码编号
     * @return 验证码图片bsse64
     */
    private String getCaptchaImage(String taskid, String captchaId) {
        logger.info("getCaptchaImage|taskid:{},captchaId:{}", taskid, captchaId);
        String url = CAPTCHA_URL.replace("ID", captchaId);
        return captchaService.getCaptchaImage(taskid, url);
    }

//
//    private String captchaLogin(MailTask mailTask, String url) {
//        String taskid = mailTask.getTaskid();
//        logger.info("captchaLogin|Enter the methdo|taskid:{},url:{}", taskid, url);
////        String url = CAPTCHA_NEXT_URL.replace("LOGINCOOKIE", encodeUrl(loginParam.getLoginCookie()))
////                .replace("SINFOCOOKIE", encodeUrl(loginParam.getsInfoCookie()))
////                .replace("PINFOCOOKIE", encodeUrl(loginParam.getpInfoCookie()))
////                .replace("ANTICSRFCOOKIE", encodeUrl(loginParam.getAntiCsrfCookie()))
////                .replaceAll("USERNAME", mailTask.getAccount().split("@")[0]);
//        HttpClientRequest request = new HttpClientRequest();
//        request.setUrl(url);
//        request.setAccessToken(taskid);
//        request.setSaveCookie(true);
//        Map<String, String> headparms = new TreeMap<>();
//        request.setHeadparms(headparms);
//        // 请求头
//        headparms.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//        headparms.put("Accept-Encoding", "gzip, deflate, sdch");
//        headparms.put("Accept-Language", "zh-CN,zh;q=0.8");
//        headparms.put("Connection", "keep-alive");
//        headparms.put("Host", "reg.youdao.com");
//        headparms.put("Upgrade-Insecure-Requests", "1");
//        HttpClientResponse response = null;
//        String sid = null;
//        try {
//            response = httpClientFactory.get(request);
//            logger.debug("captchaLogin|response|taskid:{},statusCode:{}", taskid, response.getStatusCode());
//            if (response.getStatusCode() == HttpStatus.SC_OK) {
//                sid = response.getCookie("Coremail.sid").getValue();
//            }
//        } catch (Exception e) {
//            logger.error("captchaLogin|exception|taskid:{},response:{}", taskid, response, e);
//            sid = "";
//        }
//        logger.info("captchaLogin|result|taskid:{},sid:{}", taskid, sid);
//        return sid;
//    }

//    private static String encodeUrl(String str) {
//        String result = null;
//        try {
//            result = URLEncoder.encode(str, "utf-8");
//        } catch (Exception e) {
//            logger.error("encodeUrl|exception|str", str, e);
//            result = "";
//        }
//        return result;
//    }


    /**
     * 获取邮件列表
     *
     * @param mailTask 邮件任务
     * @param sid      登录返回sid
     * @return 邮件列表
     */
    private List<Mail163Info> getMailList(MailTask mailTask, String sid) {
        String taskid = mailTask.getTaskid();
        logger.info("getMailList|Enter the method|taskid:{},sid:{}", taskid, sid);
        List<Mail163Info> mail163Infos = new ArrayList<>();
        int pageno = 1;
        boolean stop = false;
        for (; ; ) {
            logger.info("getMailList|taskid:{},pageno:{}", taskid, pageno);
            Mail163ListResp mail163Resp = getMailListPage(mailTask, sid, pageno);
            if (mail163Resp != null) {
                List<Mail163Info> pageMailInfos = mail163Resp.getVar();
                if (pageMailInfos != null && !pageMailInfos.isEmpty()) {
                    if (pageno == 1) {
                        // 取最大一条记录的时间
                        setCrawlTime(mailTask, MyUtil.dateStr2Timestamp(parseDate(pageMailInfos.get(0).getReceivedDate())));
                    }
                    for (Mail163Info mail163Info : pageMailInfos) {
                        String receivedDate = parseDate(mail163Info.getReceivedDate());
                        // 过滤邮件，超过时间的就不要
                        if (MyUtil.dateStr2Timestamp(receivedDate) <= mailTask.getLastCrawlTime()) {
                            // 已经查询过
                            stop = true;
                            break;
                        }
                        String subject = mail163Info.getSubject();
                        if (CrawlerHelper.matchSubject(subject)) {
                            mail163Info.setSentDate(parseDate(mail163Info.getSentDate()));
                            mail163Info.setReceivedDate(receivedDate);
                            mail163Infos.add(mail163Info);
                        }
                    }
                }
                if (stop || CRAWL_PAGE_SIZE * pageno >= mail163Resp.getTotal()) {
                    // 爬取完毕
                    break;
                }
            } else {
                break;
            }
            pageno++;
        }
        logger.info("getMailList|result|taskid:{},mail163Infos.size:{}", taskid, mail163Infos.size());
        return mail163Infos;
    }

    /**
     * 根据页码获取邮件列表
     *
     * @param mailTask 邮件任务
     * @param sid      登录返回值sid
     * @param pageno   页码
     * @return 邮件列表数据
     */
    private Mail163ListResp getMailListPage(MailTask mailTask, String sid, int pageno) {
        String taskid = mailTask.getTaskid();
        logger.info("getMailListPage|Enter the method|taskid:{},sid:{},pageno:{}", taskid, sid, pageno);
        String mailType = mailTask.getType();
        String url = MAIL_LIST_URL.replace("MAIL_TYPE", mailType).replace("SID", sid);
        HttpPost httpPost = new HttpPost(url);
        // 请求头信息
        httpPost.addHeader("Accept", "text/javascript");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Host", "mail." + mailType);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        httpPost.addHeader("Referer", "http://mail." + mailType + "/js6/main.jsp?sid=" + sid + "&df=email" + mailType.split("@")[0]);
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
        List<NameValuePair> paramList = new ArrayList<>();
        String formParam = "<?xml version=\"1.0\"?><object><int name=\"fid\">1</int><string name=\"order\">date</string><boolean name=\"desc\">true</boolean><int name=\"limit\">LIMIT</int><int name=\"start\">START</int><boolean name=\"skipLockedFolders\">false</boolean><string name=\"topFlag\">top</string><boolean name=\"returnTag\">true</boolean><boolean name=\"returnTotal\">true</boolean></object>";
        int start = CRAWL_PAGE_SIZE * (pageno - 1);
        formParam = formParam.replace("LIMIT", String.valueOf(CRAWL_PAGE_SIZE)).replace("START", String.valueOf(start));
        paramList.add(new BasicNameValuePair("var", formParam));
        // 获取数据
        Mail163ListResp mail163Resp = null;
        HttpData response = null;
        for (int i = 1; i <= Consts.TRY_TIMES; i++) {
            logger.debug("getMailListPage|taskid:{},url:{},pageno:{},times:{}", taskid, url, pageno, i);
            try {
                response = httpClientFactory.execute(taskid, httpPost);
                String entityStr = response.getData();
                logger.debug("getMailListPage|taskid:{},entityStr:{}", taskid, entityStr);
                if (StringUtils.isNotBlank(entityStr)) {
                    mail163Resp = parseMailListPage(taskid, entityStr);
                    break;
                }
            } catch (Exception e) {
                logger.error("getMail|exception|taskid:{},url:{},pageno:{},times:{},response:{}", taskid, url, pageno, i, response, e);
            }
            if (mail163Resp == null && i == Consts.TRY_TIMES) {
                logger.info("getMailListPage|try all fail|taskid:{},url:{}", taskid, url);
                //throw new CrawlMailException("getMailListPage fail,taskid:" + mailTask.getTaskid());
            }
        }
        return mail163Resp;
    }

    /**
     * 获取邮件数据
     *
     * @param mailTask 邮件任务
     * @param sid      登录返回值sid
     * @param mid      邮件编号
     * @return 邮件html数据
     */
    private String getMail(MailTask mailTask, String sid, String mid) {
        String taskid = mailTask.getTaskid();
        logger.info("getMail|Enter the method|taskid:{},sid:{},mid:{}", taskid, sid, mid);
        String mailType = mailTask.getType();
        String url = MAIL_URL.replace("MAIL_TYPE", mailType).replace("MID", mid);
        HttpPost httpPost = new HttpPost(url);
        // httpPost.setc("GBK");
        // 请求头信息
        httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpPost.addHeader("Accept-Encoding", "gzip, deflate");
        httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        httpPost.addHeader("Connection", "keep-alive");
        httpPost.addHeader("Host", "mail." + mailType);
        httpPost.addHeader("Referer", "http://mail." + mailType + "/js6/main.jsp?sid=" + sid + "&df=email" + mailType.split("@")[0]);
        httpPost.addHeader("Cookie", "Coremail.sid=" + sid + ";");
        httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
        // 获取邮件数据
        String mailHtml = null;
        HttpData response = null;
        for (int i = 1; i <= Consts.TRY_TIMES; i++) {
            logger.debug("getMail|taskid:{},url:{},times:{}", taskid, url, i);
            try {
                response = httpClientFactory.execute(taskid, httpPost);
                String entityStr = response.getData();
                logger.debug("getMail|taskid:{},entityStr:{}", taskid, entityStr);
                if (StringUtils.isNotBlank(entityStr)) {
                    mailHtml = CrawlerHelper.wrapHtml(entityStr);
                    break;
                }
            } catch (Exception e) {
                logger.error("getMail|exception|taskid:{},url:{},times:{},response:{}", taskid, url, i, response, e);
            }
            if (mailHtml == null && i == Consts.TRY_TIMES) {
                logger.info("getMail|try all fail|taskid:{},url:{}", taskid, url);
                //throw new CrawlMailException("getMail fail,taskid:" + taskid);
            }
        }
        return mailHtml;
    }

    /**
     * 解析邮件列表数据
     *
     * @param str 类似json数据
     * @return 邮件列表信息
     */
    private Mail163ListResp parseMailListPage(String taskid, String str) {
        logger.info("parseMailListPage|Enter the method.");
//        String json = str.replaceAll("\\\\\"", "").replaceAll("\\\\'", "").replaceAll("\\\\", "")
//                .replaceAll("\"", "").replaceAll("'", "\"").replaceAll("new Date\\(", "\"").replaceAll("\\)(?=,|\\})", "\"");
        String json = str.replaceAll("new Date\\(", "\"").replaceAll("\\)(?=,|\\})", "\"");
        Mail163ListResp mail163Resp = null;
        if (!StringUtils.isBlank(json)) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            mail163Resp = gson.fromJson(json, Mail163ListResp.class);
            logger.debug("parseMailListPage|taskid:{},mail163Resp:{}", taskid, mail163Resp);
        }
        return mail163Resp;
    }

    /**
     * 解析日期
     *
     * @param dateStr 原始日期
     * @return 格式化日期
     */
    private String parseDate(String dateStr) {
        String formmatDate = "";
        try {
            Date date;
            if (dateStr.split(",").length > 3) {
                date = sdfParser.parse(dateStr);
            } else {
                date = sdfDateParser.parse(dateStr);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.MONTH, 1);
            formmatDate = sdfFormatter.format(cal.getTime());
        } catch (Exception e) {
            logger.error("parseDate|exception|dateStr:{}", dateStr, e);
            formmatDate = "1970" + Consts.DATE_SEPARATOR + "01" + Consts.DATE_SEPARATOR + "01" + " 00:00:00";
        }
        return formmatDate;
    }


    // 测试用
    public void main(String[] args) throws IOException, ParseException {
        test();
    }

    // 测试用
    private static void test() throws IOException, ParseException {
        String classpath = "d:/work/zz/mail";
        String path = classpath + "/mail_163_list.txt";
        File file = new File(path);
        Reader reader = new FileReader(file);
        String responseStr = FileCopyUtils.copyToString(reader);
        Mail163CaptchaCrawler service = new Mail163CaptchaCrawler();
        logger.info("Mail163ListResp:{}", new Gson().toJson(service.parseMailListPage("", responseStr)));
    }
}
