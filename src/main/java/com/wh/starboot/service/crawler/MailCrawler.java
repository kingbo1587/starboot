package com.wh.starboot.service.crawler;


import com.wh.starboot.bean.MailInfo;
import com.wh.starboot.bean.MailTask;

import java.util.List;

/**
 * Created by shenjinbo on 2016/7/16
 */
public interface MailCrawler {

    /**
     * 登录邮箱
     *
     * @param mailTask 邮件任务
     * @return 登录返回值
     */
    Object loginMail(MailTask mailTask);

    /**
     * 爬取邮件
     *
     * @param mailTask  邮件任务
     * @param loginData 登录返回值
     * @return 邮件列表
     */
    List<MailInfo> crawlMails(MailTask mailTask, Object loginData);

    /**
     * 设置爬取时间
     *
     * @param mailTask  邮件任务
     * @param crawlTime 爬取时间
     */
    void setCrawlTime(MailTask mailTask, long crawlTime);
}