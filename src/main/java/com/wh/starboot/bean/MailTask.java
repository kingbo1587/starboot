package com.wh.starboot.bean;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by on 2016/7/16.
 */
@Document(collection = "mailTask")
@CompoundIndexes({
        @CompoundIndex(name = "mailTask_index", def = "{'taskid':1}", unique = true),
        @CompoundIndex(name = "mailTask_userid_index", def = "{'userid':1}"),
        @CompoundIndex(name = "mailTask_account_index", def = "{'account':1}"),
        @CompoundIndex(name = "mailTask_crawlTime_index", def = "{'crawlTime':-1}")
})
public class MailTask {
    /**
     * 任务编号
     */
    private String taskid;
    /**
     * 用户标识
     */
    private String userid;
    /**
     * 身份证号
     */
    private String idcardNo;
    /**
     * 身份证姓名
     */
    private String idcardName;
    /**
     * 邮箱号
     */
    private String account;
    /**
     * 邮箱密码
     */
    private String password;
    /**
     * 邮箱独立密码（qq邮箱使用）
     */
    private String separatePwd;
    /**
     * 验证码
     */
    private String captcha;
    /**
     * 验证码图片
     */
    private String captchaImage;
    /**
     * 登录参数
     */
    private String loginParam;
    /**
     * 任务状态
     */
    private String status;
    /**
     * 邮箱类型
     */
    private String type;
    /**
     * 爬取时间戳
     */
    private long crawlTime;
    /**
     * 上次爬取时间
     */
    private long lastCrawlTime;
    /**
     * 授信项id
     */
    private String recordAuthId;
    /**
     * 创建时间（毫秒数）
     */
    private long createtime;

    private String channel;

    private String channelSub;

    private String provider;

    /**
     * 融360返回的字符串
     * @return
     */
    private String crawlData;

    public String getCrawlData() {
        return crawlData;
    }

    public void setCrawlData(String crawlData) {
        this.crawlData = crawlData;
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getIdcardNo() {
        return idcardNo;
    }

    public void setIdcardNo(String idcardNo) {
        this.idcardNo = idcardNo;
    }

    public String getIdcardName() {
        return idcardName;
    }

    public void setIdcardName(String idcardName) {
        this.idcardName = idcardName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSeparatePwd() {
        return separatePwd;
    }

    public void setSeparatePwd(String separatePwd) {
        this.separatePwd = separatePwd;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaImage() {
        return captchaImage;
    }

    public void setCaptchaImage(String captchaImage) {
        this.captchaImage = captchaImage;
    }

    public String getLoginParam() {
        return loginParam;
    }

    public void setLoginParam(String loginParam) {
        this.loginParam = loginParam;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getCrawlTime() {
        return crawlTime;
    }

    public void setCrawlTime(long crawlTime) {
        this.crawlTime = crawlTime;
    }

    public long getLastCrawlTime() {
        return lastCrawlTime;
    }

    public void setLastCrawlTime(long lastCrawlTime) {
        this.lastCrawlTime = lastCrawlTime;
    }

    public String getRecordAuthId() {
        return recordAuthId;
    }

    public void setRecordAuthId(String recordAuthId) {
        this.recordAuthId = recordAuthId;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelSub() {
        return channelSub;
    }

    public void setChannelSub(String channelSub) {
        this.channelSub = channelSub;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "MailTask{" +
                "taskid='" + taskid + '\'' +
                ", userid='" + userid + '\'' +
                ", idcardNo='" + idcardNo + '\'' +
                ", idcardName='" + idcardName + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", separatePwd='" + separatePwd + '\'' +
                ", captcha='" + captcha + '\'' +
                ", captchaImage='" + captchaImage + '\'' +
                ", loginParam='" + loginParam + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", crawlTime=" + crawlTime +
                ", lastCrawlTime=" + lastCrawlTime +
                ", recordAuthId='" + recordAuthId + '\'' +
                ", createtime=" + createtime +
                ", channel='" + channel + '\'' +
                ", channelSub='" + channelSub + '\'' +
                ", provider='" + provider + '\'' +
                '}';
    }
}
