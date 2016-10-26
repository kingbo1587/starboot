package com.wh.starboot.bean.mail163;

/**
 * Created by  on 2016/7/16.
 */
public class Mail163Info {
    /**
     * 邮件编号
     */
    private String id;
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
     * 发送时间
     */
    private String sentDate;
    /**
     * 接收时间
     */
    private String receivedDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "MailInfo{" +
                "id='" + id + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", subject='" + subject + '\'' +
                ", sentDate='" + sentDate + '\'' +
                ", receivedDate='" + receivedDate + '\'' +
                '}';
    }
}
