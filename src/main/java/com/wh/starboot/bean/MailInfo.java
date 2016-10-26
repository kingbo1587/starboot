package com.wh.starboot.bean;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by kingbo on 2016/7/16.
 */
@Document(collection = "mailInfo")
@CompoundIndexes({
        @CompoundIndex(name = "mailInfo_taskid_index", def = "{'taskid':1}"),
        @CompoundIndex(name = "mailInfo_account_index", def = "{'account':1}")
})
public class MailInfo {

    /**
     * 任务编号
     */
    private String taskid;
    /**
     * 邮箱账号
     */
    private String account;
    /**
     * 用户标识
     */
    private String userid;
    /**
     * 邮箱类型
     */
    private String mailType;
    /**
     * 发件人
     */
    private String from;
    /**
     * 收件人
     */
    private String to;
    /**
     * 邮件主题
     */
    private String subject;
    /**
     * 发送时间(yyyy/MM/dd HH:mm:ss)
     */
    private String sentDate;
    /**
     * 收取时间(yyyy/MM/dd HH:mm:ss)
     */
    private String receivedDate;
    /**
     * 邮件内容数据
     */
    private String mailData;

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSentDate() {
        return sentDate;
    }

    public void setSentDate(String sentDate) {
        this.sentDate = sentDate;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getMailData() {
        return mailData;
    }

    public void setMailData(String mailData) {
        this.mailData = mailData;
    }

    @Override
    public String toString() {
        return "MailInfo{" +
                "taskid='" + taskid + '\'' +
                ", account='" + account + '\'' +
                ", userid='" + userid + '\'' +
                ", mailType='" + mailType + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate='" + sentDate + '\'' +
                ", receivedDate='" + receivedDate + '\'' +
                '}';
    }
}
