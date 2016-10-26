package com.wh.starboot.dao;


import com.wh.starboot.bean.MailInfo;
import com.wh.starboot.bean.MailTask;
import com.wh.starboot.bean.TaskStatus;

import java.util.List;

/**
 * Created by  on 2016/7/16.
 */
public interface MailDao {

    void saveTask(MailTask mailTask);

    MailTask getTask(String taskid);

    void updateTask(MailTask mailTask, TaskStatus taskStatus);

    void updateTaskCrawlTime(MailTask mailTask);

    void updateTaskRecordAuthId(MailTask mailTask);

    void updateTask(MailTask mailTask, String captchaImage, String loginParam, TaskStatus taskStatus);

    void updateTaskProvider(MailTask mailTask, String provider);

    long getLastCrawlTime(MailTask mailTask);

    void batchInsertMailInfo(List<MailInfo> list);

    List<MailInfo> getMailInfos(String taskid);

    void updateTaskCrawlData(MailTask mailTask, String crawlData);
}
